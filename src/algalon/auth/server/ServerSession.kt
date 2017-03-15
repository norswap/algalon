package algalon.auth.server
import algalon.utils.BigUnsigned
import algalon.auth.Version
import algalon.auth.server.ServerSession.Status.*
import algalon.database.ChilledSessions
import algalon.database.User
import algalon.utils.HasStateString
import algalon.utils.b
import algalon.utils.net.Socket
import algalon.utils.net.SocketHook
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.security.MessageDigest

/**
 * Container for data related to an authentication attempt on the server side.
 */
class ServerSession (val server: AuthServer, val sock: Socket): HasStateString, SocketHook
{
    // ---------------------------------------------------------------------------------------------

    val sbuf: ByteBuffer = ByteBuffer.allocate(256)
    val rbuf: ByteBuffer = ByteBuffer.allocate(256)

    // NOTE: if this is only ever used in a single callback, make local
    val sha1 by lazy { MessageDigest.getInstance("SHA-1") }

    init {
        server.count.incrementAndGet()
        sbuf.order(LITTLE_ENDIAN)
        rbuf.order(LITTLE_ENDIAN)
        rbuf.limit(0)
    }

    // ---------------------------------------------------------------------------------------------
    // Session State

    enum class Status {
        // order matters
        INITIAL,
        SENT_CHALLENGE,
        SENT_RECONNECT_CHALLENGE,
        SENT_PROOF,
        SENT_RECONNECT_PROOF
    }

    var status = INITIAL
    var offensive_close = false

    // ---------------------------------------------------------------------------------------------
    // User Data

    /** Submitted username. */
    var username: String? = null

    /** The user that has authenticated. */
    lateinit var user: User

    /** Client version. */
    lateinit var version: Version

    // ---------------------------------------------------------------------------------------------
    // Data to be preserved between callbacks.

    var len = 0
    var username_len = 0
    var challenge_opcode = 0.b
    lateinit var b1: BigUnsigned
    lateinit var B2: BigUnsigned
    lateinit var random_challenge: ByteArray

    // ---------------------------------------------------------------------------------------------

    override fun toString(): String
    {
        val id = server.id
        return username?.let { "sauth($id, $it)" } ?: "sauth($id)"
    }

    // ---------------------------------------------------------------------------------------------

    // TODO these things may not be initialized
    override fun state_string()
        = "ServerSession {\n" +
        "    state: $status,\n" +
        "    version: $version,\n" +
        "    user: $username,\n" +
        "}"

    // ---------------------------------------------------------------------------------------------

    override fun close_hook()
    {
        server.count.decrementAndGet()

        if (status.ordinal > SENT_PROOF.ordinal && !offensive_close)
            ChilledSessions.chill(username!!, user.K!!)
    }

    // ---------------------------------------------------------------------------------------------
}
