package algalon.logon.server
import algalon.utils.BigUnsigned
import algalon.logon.Version
import algalon.logon.server.Session.Status.*
import algalon.database.ChilledSessions
import algalon.database.Account
import algalon.database.DatabaseSession
import algalon.logon.Opcode
import algalon.utils.net.Socket
import algalon.utils.net.SocketHook
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN

/**
 * Container for data related to an logon attempt on the server side.
 */
class Session (val server: Server, val sock: Socket): DatabaseSession(), SocketHook
{
    // ---------------------------------------------------------------------------------------------

    var sbuf: ByteBuffer = ByteBuffer.allocate(256)
    val rbuf: ByteBuffer = ByteBuffer.allocate(256)

    /** Whether the session should be traced. */
    var trace = false

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
        CONNECTED
    }

    var status = INITIAL
    var offensive_close = false

    // ---------------------------------------------------------------------------------------------
    // User Data

    /** Submitted username. */
    var username: String? = null

    /** The user that has authenticated. */
    lateinit var account: Account

    /** Client version. */
    lateinit var version: Version

    // ---------------------------------------------------------------------------------------------
    // Data to be preserved between callbacks.

    var len = 0
    var username_len = 0
    var challenge_opcode = Opcode.LOGON_CHALLENGE
    lateinit var b1: BigUnsigned
    lateinit var B2: BigUnsigned
    lateinit var random_challenge: ByteArray

    // ---------------------------------------------------------------------------------------------

    val log_header: String
        get() {
            val id = server.id
            return username?.let { "slogon($id, $it)" } ?: "slogon($id)"
        }

    // ---------------------------------------------------------------------------------------------

    // TODO these things may not be initialized
    override fun toString()
        = "Session {\n" +
        "    state: $status,\n" +
        //"    version: $version,\n" +
        "    user: $username,\n" +
        "}"

    // ---------------------------------------------------------------------------------------------

    override fun close_hook()
    {
        server.count.decrementAndGet()

        if (status.ordinal > CONNECTED.ordinal && !offensive_close)
            ChilledSessions.chill(username!!, account.session_key!!)
    }

    // ---------------------------------------------------------------------------------------------
}
