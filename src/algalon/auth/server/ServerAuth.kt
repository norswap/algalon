package algalon.auth.server
import algalon.auth.*
import algalon.auth.crypto.*
import algalon.auth.err.*
import algalon.auth.op.*
import algalon.database.User
import algalon.database.Users
import algalon.settings.*
import algalon.utils.*
import algalon.utils.net.*
import kotlin.text.Charsets.UTF_8
import org.pmw.tinylog.Logger

// The famous secret Xi Chi fraternity handshake.

////////////////////////////////////////////////////////////////////////////////////////////////////
// Helpers

private fun ServerSession.read (read: Int, callback: () -> Unit)
{
    sock.read(read, rbuf, this, callback)
}

// -------------------------------------------------------------------------------------------------

private fun ServerSession.write (callback: () -> Unit)
{
    sbuf.flip()
    sock.write(sbuf, this, callback)
}

// -------------------------------------------------------------------------------------------------

private fun ServerSession.die (msg: String)
{
    Logger.warn(msg, this)
    sock.close(this)
}

// -------------------------------------------------------------------------------------------------

@Suppress("NOTHING_TO_INLINE")
private inline fun ServerSession.trace_auth (msg: String)
{
    if (TRACE_AUTH) Logger.trace("[$this] $msg")
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ServerSession.write_error (opcode: Byte, errcode: Byte)
{
    when (opcode) {
        LOGON_CHALLENGE ->
            sbuf.put(opcode, 0, errcode)
        LOGON_PROOF ->
            if (version.build > 6005) // > 1.12.2
                // 1.12.3 (chinese client patch) or extensions
                // (not verified on 1.12.3)
                sbuf.put(opcode, errcode, 3, 0)
            else
                sbuf.put(opcode, errcode)
        RECONNECT_CHALLENGE ->
            sbuf.put(opcode, errcode)
    }

    // NOTE: Whenever we send an error, the Vanilla client closes the connection.
    // However, Trinity/Mangos return to packet accept.
    // Maybe the behaviour is different for some expansions?

    write { sock.close(this) }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ServerSession.rate_bookkeeping()
{
    // Each IP address is allowed 1 connection attempt per second (starting with 10).
    // Establish more and you get a 100 seconds penality for each additional connection.

    val now = now
    val info = server.rate_book.getOrPut(sock.host) { AuthServer.IPInfo(now, 10) }
    info.budget += now - info.timestamp - 1
    info.timestamp = now
    if (info.budget > 10) info.budget = 10 // disallow hoarding budget
    if (info.budget < 0) {
        info.budget -= 100 // penalize
        die("connection rate exceeded: {}")
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ServerSession.receive_packet()
    = read(1) {
        val opcode = rbuf.get()
        when (opcode) {
            LOGON_CHALLENGE -> {
                rate_bookkeeping()
                read(CLOGON_CHALLENGE_FIXED_LENGTH - 1) {
                    trace_auth("received client challenge")
                    user_handling = ServerSession::handle_user
                    handle_client_challenge()
                }
            }
            LOGON_PROOF -> {
                read (CLOGON_PROOF_LENGTH - 1) { handle_client_proof() }
            }
            RECONNECT_CHALLENGE -> {
                rate_bookkeeping()
                read (CRECONNECT_CHALLENGE_FIXED_LENGTH - 1) {
                    trace_auth("received client reconnect challenge")
                    user_handling = ServerSession::handle_user_reconnect
                    handle_client_challenge()
                }
            }
            RECONNECT_PROOF -> {
                read (CRECONNECT_PROOF_LENGTH - 1) { handle_reconnect_proof() }
            }
            else -> {
                // no logging (avoid attacks)
                sock.close()
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ServerSession.handle_client_challenge()
{
    if (sent_challenge || sent_reconnect_challenge)
        return die("second challenge: {}")

    rbuf.skip(1) // skip unknown
    len = rbuf.ushort

    if (len > CLOGON_CHALLENGE_MAX_LENGTH)
        return die ("bogus length: {}")

    rbuf.skip(4) // skip game name
    version = version(rbuf.byte, rbuf.byte, rbuf.byte, rbuf.short.i)

    if (version is InvalidVersion) {
        Logger.info("invalid version ({}): {}", version, this)
        return write_error(LOGON_CHALLENGE, AUTH_FAIL_VERSION_INVALID)
    }

    rbuf.skip(20)
    username_len = rbuf.byte

    if (CLOGON_CHALLENGE_FIXED_LENGTH - 4 + username_len != len)
        return die ("inconsistent lengths: {}")

    if (username_len > MAX_USERNAME_LEN) {
        Logger.warn("username too long: {}", this)
        return write_error(LOGON_CHALLENGE, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    read (username_len) { handle_username() }
}

// -------------------------------------------------------------------------------------------------

fun ServerSession.handle_username()
{
    if (rbuf.remaining() != username_len)
        return die("crap at end of logon packet: {}")

    val array = ByteArray(username_len)
    rbuf.get(array)
    username = array.toString(UTF_8)

    trace_auth("auth initiated: $username")

    Users.load(username!!) { user_handling?.invoke(this, it) }
}

// -------------------------------------------------------------------------------------------------

fun ServerSession.handle_user (user: User?)
{
    if (user == null) {
        trace_auth("unknown username")
        return write_error(LOGON_CHALLENGE, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    this.user = user
    b1 = BigUnsigned.random(19)
    B2 = (k * user.verifier + g.exp_mod(b1, N)) % N
    trace_auth("B = $B2")

    // all servers do it like this
    // not sure what sending all 0s would entail
    val ubytes = ByteArray(16)
    RANDOM.nextBytes(ubytes)

    sbuf.put(LOGON_CHALLENGE) // opcode
    sbuf.put(0) // unknown
    sbuf.put(AUTH_SUCCESS) // error
    sbuf.put(B2.bytes(32))
    sbuf.put(1)  // g length
    sbuf.put(g.bytes) // g
    sbuf.put(32) // N length
    sbuf.put(N.bytes)
    sbuf.put(user.salt)
    sbuf.put(ubytes) // unknown
    sbuf.put(0) // security flags

    write {
        sent_challenge = true
        trace_auth("sent server challenge")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ServerSession.handle_client_proof()
{
    trace_auth("received client proof")

    if (!sent_challenge)
        return die("out of order proof: {}")

    if (sent_proof)
        return die("second proof: {}")

    val A = rbuf.big_unsigned(32)
    val M1c = rbuf.big_unsigned(20)

    // skip crc_hash, number of keys & security flags
    rbuf.skip(22)

    trace_auth("A = $A")

    if (A.is_zero)
        return die("zero SRP6 A value: {}")

    val u = BigUnsigned(sha1.digest(A.bytes, B2.bytes))
    val S = (A * user.verifier.exp_mod(u, N)).exp_mod(b1, N)
    val K = session_key_hash(S)
    val M1s = M1(sha1, username!!.utf8, user.salt, A, B2, K)

    trace_auth("S = $S")
    trace_auth("salt: " + user.salt.hex_string)
    trace_auth("client M1 = $M1c")
    trace_auth("server M1 = $M1s")

    if (M1c != M1s) {
        Logger.info("invalid logon proof: {}", this)
        return write_error(LOGON_PROOF, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    user.K = K
    val M2 = sha1.digest(A.bytes, M1c.bytes, K.bytes)

    // NOTE: packet format differs in 2.x and 3.x
    sbuf.put(LOGON_PROOF) // opcode
    sbuf.put(AUTH_SUCCESS)
    sbuf.put(M2)
    if (version.major > 1) {
        sbuf.putInt(ACCOUNT_FLAG_PROPASS)
        sbuf.putInt(0) // survey id
        sbuf.putInt(0) // unknown flags
    }
    else {
        sbuf.putInt(0) // unknown
    }

    write {
        sent_proof = true
        trace_auth("sent server proof")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ServerSession.handle_user_reconnect (user: User?)
{
    if (user == null) {
        trace_auth("unknown username")
        return write_error(RECONNECT_CHALLENGE, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    this.user = user

    if (user.K == null) {
        trace_auth("unknown session")
        // TODO maybe it's better to simply die?
        return write_error(RECONNECT_CHALLENGE, AUTH_FAIL_UNKNOWN1)
    }

    // all servers do it like this
    // not sure what sending all 0s would entail
    reconnect_random = ByteArray(16)
    RANDOM.nextBytes(reconnect_random)

    sbuf.put(RECONNECT_CHALLENGE) // opcode
    sbuf.put(AUTH_SUCCESS) // error
    sbuf.put(reconnect_random)
    sbuf.put(ByteArray(16)) // zeroed

    write {
        sent_reconnect_challenge = true
        trace_auth("sent server reconnect challenge")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ServerSession.handle_reconnect_proof()
{
    trace_auth("received client reconnect proof")

    if (!sent_reconnect_challenge)
        return die("out of order reconnect proof: {}")

    if (sent_reconnect_proof)
        return die("second reconnect proof: {}")

    val R1  = rbuf.big_unsigned(16)
    val R2c = rbuf.big_unsigned(20)
    //rbuf.skip(20) // unused R3
    val R3 = rbuf.big_unsigned(20)
    sha1.update(user.username.utf8)
    sha1.update(R1.bytes)
    sha1.update(reconnect_random)
    sha1.update(user.K!!.bytes)
    val R2s = BigUnsigned(sha1.digest())

    trace_auth("R1 = $R1")
    trace_auth("client R2 = $R2c")
    trace_auth("server R2 = $R2s")
    trace_auth("R3 = $R3")
    trace_auth("random = ${BigUnsigned(reconnect_random)}")

    if (R2c != R2s)
        return die("invalid reconnect logon proof: {}")

    sbuf.put(RECONNECT_PROOF) // opcode
    sbuf.put(AUTH_SUCCESS) // error
    if (version.major > 1)
        sbuf.putShort(0) // unknown

    write {
        sent_reconnect_proof = true
        trace_auth("sent server reconnect proof")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////