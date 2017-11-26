@file:Suppress("NON_EXHAUSTIVE_WHEN")
package algalon.logon.server
import algalon.utils.Clock
import algalon.logon.*
import algalon.logon.crypto.*
import algalon.logon.lengths.*
import algalon.logon.Opcode
import algalon.logon.Opcode.*
import algalon.logon.Errcode
import algalon.logon.Errcode.*
import algalon.logon.server.Session.Status.*
import algalon.database.Account
import algalon.database.Users
import algalon.logon.realm.Realm
import algalon.logon.realm.RealmFlag
import algalon.utils.*
import algalon.utils.net.*
import kotlin.text.Charsets.UTF_8
import org.pmw.tinylog.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

// The famous secret Xi Chi fraternity handshake.

////////////////////////////////////////////////////////////////////////////////////////////////////
// Helpers

private fun Session.read (read: Int, timeout: Long = server.conf.read_timeout, callback: () -> Unit)
{
    sock.read(read, rbuf, this, timeout, callback)
}

// -------------------------------------------------------------------------------------------------

private fun Session.write (timeout: Long = server.conf.write_timeout, callback: () -> Unit)
{
    sbuf.flip()
    sock.write(sbuf, this, timeout, callback)
}

// -------------------------------------------------------------------------------------------------

private fun Session.die (msg: String)
{
    Logger.warn(msg, this)
    offensive_close = true
    sock.close(this)
}

// -------------------------------------------------------------------------------------------------

@Suppress("NOTHING_TO_INLINE")
private inline fun Session.trace (msg: String)
{
    if (trace) Logger.trace("[$log_header] $msg")
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.write_error (opcode: Opcode, errcode: Errcode)
{
    when (opcode) {
        Opcode.LOGON_CHALLENGE ->
            sbuf.put(opcode.b, 0, errcode.b)
        LOGON_PROOF ->
            if (version.build > 6005) // > 1.12.2
                // 1.12.3 (chinese client patch) or extensions
                // (not verified on 1.12.3)
                sbuf.put(opcode.b, errcode.b, 3, 0)
            else
                sbuf.put(opcode.b, errcode.b)
        RECONNECT_CHALLENGE ->
            sbuf.put(opcode.b, errcode.b)
    }

    // NOTE: Whenever we send an error, the Vanilla client closes the connection.
    // However, Trinity/Mangos return to packet accept.
    // Maybe the behaviour is different for some expansions?

    write { sock.close(this) }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.rate_bookkeeping()
{
    // Each IP address is allowed 1 connection attempt per second (starting with 10).
    // Establish more and you get a 100 seconds penality for each additional connection.

    val now = Clock.timestamp
    val info = server.rate_book.getOrPut(sock.host) { Server.IPInfo(now, 10) }
    info.budget += now - info.timestamp - 1
    info.timestamp = now
    if (info.budget > 10) info.budget = 10 // disallow hoarding budget
    if (info.budget < 0) {
        info.budget -= 100 // penalize
        die("connection rate exceeded: {}")
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.receive_packet (timeout: Long = server.conf.read_timeout)
    = read(1, timeout) {
        val opcode = rbuf.get()
        trace("opcode = $opcode")
        when (opcode) {
            LOGON_CHALLENGE.b -> {
                rate_bookkeeping()
                read(CLOGON_CHALLENGE_FIX_LENGTH - 1) {
                    trace("received client challenge")
                    challenge_opcode = LOGON_CHALLENGE
                    handle_client_challenge()
                }
            }
            LOGON_PROOF.b -> {
                read (CLOGON_PROOF_LENGTH - 1) { handle_client_proof() }
            }
            RECONNECT_CHALLENGE.b -> {
                rate_bookkeeping()
                read (CRECONNECT_CHALLENGE_FIX_LENGTH - 1) {
                    trace("received client reconnect challenge")
                    challenge_opcode = RECONNECT_CHALLENGE
                    handle_client_challenge()
                }
            }
            RECONNECT_PROOF.b -> {
                read (CRECONNECT_PROOF_LENGTH - 1) { handle_reconnect_proof() }
            }
            REALM_LIST.b -> {
                read (CREALM_LIST_REQUEST_LENGTH - 1) { handle_realmlist() }
            }
            else -> {
                // no logging (avoid attacks)
                sock.close(this)
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.handle_client_challenge()
{
    if (status != INITIAL)
        return die("out of order challenge: {}")

    rbuf.skip(1) // skip unknown
    len = rbuf.ushort

    if (len > CLOGON_CHALLENGE_FIX_LENGTH + server.conf.max_username_length)
        return die ("bogus length: {}")

    rbuf.skip(4) // skip game name
    version = Version(rbuf.byte, rbuf.byte, rbuf.byte, rbuf.short.i)

    if (!server.conf.accepted_builds.contains(version)) {
        Logger.info("invalid version ({}): {}", version, this)
        return write_error(LOGON_CHALLENGE, AUTH_FAIL_VERSION_INVALID)
    }

    rbuf.skip(20)
    username_len = rbuf.byte

    if (CLOGON_CHALLENGE_FIX_LENGTH - 4 + username_len != len)
        return die ("inconsistent lengths: {}")

    if (username_len > server.conf.max_username_length) {
        Logger.warn("username too long: {}", this)
        return write_error(LOGON_CHALLENGE, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    read (username_len) { handle_username() }
}

// -------------------------------------------------------------------------------------------------

fun Session.handle_username()
{
    if (rbuf.remaining() != username_len)
        return die("crap at end of logon packet: {}")

    username = rbuf.bytes(username_len).toString(UTF_8)
    trace("logon initiated: $username")

    Users.load(username!!) {
        when (challenge_opcode) {
            LOGON_CHALLENGE     -> handle_user(it)
            RECONNECT_CHALLENGE -> handle_user_reconnect(it)
        }
    }
}

// -------------------------------------------------------------------------------------------------

fun Session.handle_user (account: Account?)
{
    if (account == null) {
        trace("unknown username")
        return write_error(LOGON_CHALLENGE, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    this.account = account
    b1 = BigUnsigned(RANDOM, 19) // TODO unsafe
    B2 = (k * account.pwd_verifier + g.exp_mod(b1, N)) % N
    trace("B = $B2")
    random_challenge = RANDOM.bytes(16)


    sbuf.put(LOGON_CHALLENGE.b) // opcode
    sbuf.put(0) // unknown
    sbuf.put(AUTH_SUCCESS.b) // error
    sbuf.put(B2.bytes(32))
    sbuf.put(1)  // g length
    sbuf.put(g.bytes) // g
    sbuf.put(32) // N length
    sbuf.put(N.bytes)
    sbuf.put(account.pwd_salt)
    sbuf.put(random_challenge) // unknown
    sbuf.put(0) // security flags

    write {
        status = SENT_CHALLENGE
        trace("sent server challenge")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.handle_client_proof()
{
    trace("received client proof")

    if (status != SENT_CHALLENGE)
        return die("out of order proof: {}")

    val A   = rbuf.big_unsigned(32)
    val M1c = rbuf.big_unsigned(20)
    rbuf.skip(22) // crc_hash, number of keys & security flags

    if ((A % N).is_zero)
        return die("zero SRP6 (A % N) value: {}")

    trace("A = $A")

    val sha1 = MessageDigest.getInstance("SHA-1")
    val u = BigUnsigned(sha1.digest(A.bytes, B2.bytes))
    val S = (A * account.pwd_verifier.exp_mod(u, N)).exp_mod(b1, N)
    val K = session_key_hash(S)
    val M1s = M1(sha1, username!!.utf8, account.pwd_salt, A, B2, K)

    trace("S = $S")
    trace("salt: " + account.pwd_salt.hex_string)
    trace("client M1 = $M1c")
    trace("server M1 = $M1s")

    if (M1c != M1s) {
        Logger.info("invalid logon proof: {}", this)
        return write_error(LOGON_PROOF, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    account.session_key = K
    val M2 = sha1.digest(A.bytes, M1c.bytes, K.bytes)

    // NOTE: packet format differs in 2.x and 3.x
    sbuf.put(LOGON_PROOF.b) // opcode
    sbuf.put(AUTH_SUCCESS.b)
    sbuf.put(M2)
    if (version.major > 1) {
        sbuf.putInt(AccountFlag.NONE.value)
        sbuf.putInt(0) // survey id
        sbuf.putInt(0) // unknown flags
    }
    else {
        sbuf.putInt(0) // unknown
    }

    write {
        status = CONNECTED
        trace("sent server proof")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.handle_user_reconnect (account: Account?)
{
    if (account == null) {
        trace("unknown username")
        return write_error(RECONNECT_CHALLENGE, AUTH_FAIL_INVALID_CREDENTIALS1)
    }

    // TODO must set the version
    // TODO must save the version

    this.account = account

    if (account.session_key == null) {
        trace("unknown session")
        return write_error(RECONNECT_CHALLENGE, AUTH_FAIL_UNKNOWN1)
    }

    random_challenge = RANDOM.bytes(16)

    sbuf.put(RECONNECT_CHALLENGE.b) // opcode
    sbuf.put(AUTH_SUCCESS.b) // error
    sbuf.put(random_challenge)
    sbuf.put(ByteArray(16)) // zeroed

    write {
        status = SENT_RECONNECT_CHALLENGE
        trace("sent server reconnect challenge")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.handle_reconnect_proof()
{
    trace("received client reconnect proof")

    if (status != SENT_RECONNECT_CHALLENGE)
        return die("out of order reconnect proof: {}")

    val R1  = rbuf.big_unsigned(16)
    val R2c = rbuf.big_unsigned(20)
    rbuf.skip(21) // unused R3 + number of keys

    val sha1 = MessageDigest.getInstance("SHA-1")!!
    sha1.update(account.name.utf8)
    sha1.update(R1.bytes)
    sha1.update(random_challenge)
    sha1.update(account.session_key!!.bytes)
    val R2s = BigUnsigned(sha1.digest())

    trace("R1 = $R1")
    trace("client R2 = $R2c")
    trace("server R2 = $R2s")

    if (R2c != R2s)
        return die("invalid reconnect logon proof: {}")

    sbuf.put(RECONNECT_PROOF.b) // opcode
    sbuf.put(AUTH_SUCCESS.b) // error
    if (version.major > 1)
        sbuf.putShort(0) // unknown

    write {
        status = CONNECTED
        trace("sent server reconnect proof")
        receive_packet()
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun Session.handle_realmlist()
{
    if (status != CONNECTED)
        return die("out of order realm list request: {}")

    rbuf.skip(4) // unknown
    Users.with_realms(account, this::send_realmlist)
}

// -------------------------------------------------------------------------------------------------

fun Session.send_realmlist (realms: List<Realm>, character_counts: Map<Int, Int>)
{
    val old_sbuf = sbuf
    val buf_size = SREALM_LIST_MAX_FIXED_LENGTH + realms.size * SREALM_LIST_MAX_RECORD_LENGTH
    sbuf = ByteBuffer.allocate(buf_size)
    sbuf.order(ByteOrder.LITTLE_ENDIAN)

    sbuf.put(REALM_LIST.b)
    sbuf.putShort(0) // placeholder for packet size
    sbuf.putInt(0)   // unknown

    if (version.major > 1)
        sbuf.putShort(realms.size.s)
    else
        sbuf.put(realms.size.b)

    for (realm in realms)
    {
        var flags = realm.flags

        if (!realm.version(version)) {
            flags += RealmFlag.OFFLINE.value
            flags += RealmFlag.SPECIFY_BUILD.value
        }

        if (version.major > 1) {
            sbuf.put(realm.type.value)
            sbuf.put(if (realm.locked(account)) 0.b else 1.b)
        }
        else {
            sbuf.putInt(realm.type.value.i)
        }

        sbuf.put(flags.b)
        sbuf.put(realm.display_name.utf8)
        sbuf.put(0) // null terminator
        sbuf.put(realm.ip_string.utf8)
        sbuf.put(0) // null terminator
        sbuf.putFloat(realm.population.value)
        sbuf.put(character_counts[realm.id]!!.b)
        sbuf.put(realm.timezone.b)
        sbuf.put(realm.id.b)

//        // If flags include SPECIFY_BUILD, must send the build number.
//        // We don't use SPECIFY_BUILD.
//        if (version.major > 1 && specify_build) {
//            sbuf.put(version.major.b)
//            sbuf.put(version.minor.b)
//            sbuf.put(version.bugfix.b)
//            sbuf.putShort(version.build.s)
//        }
    }

    sbuf.putShort(16) // footer (unknown)
    sbuf.putShort(1, (sbuf.position() - 3).s) // set packet size

    write {
        sbuf = old_sbuf
        trace("sent realm list")
        receive_packet(60_000) // 1 min
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
