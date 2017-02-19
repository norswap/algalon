package algalon.auth.client
import algalon.auth.crypto.*
import algalon.auth.err.*
import algalon.auth.op.*
import algalon.settings.TRACE_AUTH
import algalon.utils.*
import algalon.utils.net.*
import org.pmw.tinylog.Logger

// The famous secret Xi Chi fraternity handshake.

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ClientSession.read (read: Int, callback: () -> Unit)
{
    sock.read(read, rbuf, this, callback)
}

// -------------------------------------------------------------------------------------------------

fun ClientSession.write (callback: () -> Unit)
{
    sbuf.flip()
    sock.write(sbuf, this, callback)
}

// -------------------------------------------------------------------------------------------------

@Suppress("NOTHING_TO_INLINE")
private inline fun ClientSession.trace_auth (msg: String) {
    if (TRACE_AUTH) Logger.trace("[$this] $msg")
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ClientSession.authenticate()
{
    val username = client.username.utf8

    sbuf.put(LOGON_CHALLENGE) // opcode
    sbuf.put(3) // unknown
    sbuf.putShort((CLOGON_CHALLENGE_FIXED_LENGTH - 4 + username.size).s) // length
    sbuf.put(client.game)
    sbuf.put(client.version.major.b)
    sbuf.put(client.version.minor.b)
    sbuf.put(client.version.bugfix.b)
    sbuf.putShort(client.version.build.s)
    sbuf.put(client.platform)
    sbuf.put(client.os)
    sbuf.put(client.lang)
    sbuf.putInt(client.timezone_bias)
    sbuf.put(client.ip)
    sbuf.put(client.username.length.b)
    sbuf.put(username)

    write {
        trace_auth("sent client challenge")
        read (SLOGON_CHALLENGE_MIN_LENGTH) { receive_server_challenge() }
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ClientSession.receive_server_challenge()
{
    if (rbuf.byte != LOGON_CHALLENGE.i) {
        Logger.info("wrong opcode: {}", this)
        sock.close()
        return
    }

    rbuf.skip(1) // unknown
    val err = rbuf.byte
    if (err != AUTH_SUCCESS.i) {
        Logger.info("server logon challenge error: ${errcode_name(err)}")
        sock.close()
        return
    }

    // We ignore optional security bytes, our server doesn't use them.
    read (SLOGON_CHALLENGE_REST_LENGTH) {
        trace_auth("received server challenge")
        handle_server_challenge()
    }
}

// -------------------------------------------------------------------------------------------------

fun ClientSession.handle_server_challenge()
{
    val B = rbuf.big_unsigned(32)
    val g_len = rbuf.ubyte
    val g = rbuf.big_unsigned(g_len)
    val N_len = rbuf.ubyte
    val N = rbuf.big_unsigned(N_len)
    val salt = ByteArray(32)
    rbuf.get(salt)
    rbuf.skip(16) // random bytes

    // ignore security flags
    // commented out because we don't read them in the first place
    // if they were set, they would have to be handled correctly
    /* val security_flags =*/ rbuf.byte
    /*
    if (securityFlags and 0x01 != 0) // pin input
        rbuf.skip(20)
    if (securityFlags and 0x02 != 0) // matrix input
        rbuf.skip(12)
    if (securityFlags and 0x04 != 0) // security token input
        rbuf.skip()
    */

    val username = client.username.utf8
    val a  = BigUnsigned.random(19)
    A  = g.exp_mod(a, N)
    val u  = BigUnsigned(sha1.digest(A.bytes, B.bytes))
    val x  = private_key(username, client.password.utf8, salt)
    val v  = user_verifier(x, g, N)
    val S  = (B.min_mod(k * v, N)).exp_mod(a + u * x, N)
    K  = session_key_hash(S)
    M1 = M1(sha1, username, salt, A, B, K)

    trace_auth("client A  = $A")
    trace_auth("client B  = $B")
    trace_auth("client S  = $S")

    sbuf.put(LOGON_PROOF) // opcode
    sbuf.put(A.bytes(32))
    sbuf.put(M1.bytes(20))
    sbuf.put(ByteArray(20)) // crc_hash
    sbuf.put(0) // number of keys
    sbuf.put(0) // unknown

    write {
        trace_auth("sent client proof")
        read(SLOGON_PROOF_MIN_LENGTH) { receive_server_proof() }
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////

fun ClientSession.receive_server_proof()
{
    if (rbuf.byte != LOGON_PROOF.i) {
        Logger.warn("wrong opcode: {}", this)
        sock.close()
        return
    }

    val err = rbuf.byte
    if (err != AUTH_SUCCESS.i) {
        Logger.info("server logon proof error: ${errcode_name(err)}")
        sock.close()
        return
    }

    read (SLOGON_PROOF_REST_LENGTH) {
        trace_auth("received server proof")
        handle_server_proof()
    }
}

// -------------------------------------------------------------------------------------------------

fun ClientSession.handle_server_proof()
{
    val M2s = rbuf.big_unsigned(20)
    val M2c = BigUnsigned(sha1.digest(A.bytes, M1.bytes, K.bytes))

    if (M2s != M2c) {
        Logger.warn("invalid server proof: {}", this)
        sock.close()
        return
    }

    rbuf.skip(4) // account flags
    trace_auth("validated server proof")
}

////////////////////////////////////////////////////////////////////////////////////////////////////
