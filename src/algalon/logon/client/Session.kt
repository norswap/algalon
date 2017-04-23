package algalon.logon.client
import algalon.Client
import algalon.utils.BigUnsigned
import algalon.utils.HasStateString
import algalon.utils.b
import algalon.utils.net.Socket
import org.pmw.tinylog.Logger
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture

/**
 * Container for data related to an authentication attempt on the client side.
 */
class Session(val client: Client): HasStateString
{
    // ---------------------------------------------------------------------------------------------

    lateinit var socket: Socket
    lateinit var server: InetSocketAddress

    val sha1 = MessageDigest.getInstance("SHA-1")
    val md5  = MessageDigest.getInstance("MD5")

    val sbuf: ByteBuffer = ByteBuffer.allocate(256)
    val rbuf: ByteBuffer = ByteBuffer.allocate(256)

    // ---------------------------------------------------------------------------------------------

    init {
        sbuf.order(LITTLE_ENDIAN)
        rbuf.order(LITTLE_ENDIAN)
        rbuf.limit(0)
    }

    // ---------------------------------------------------------------------------------------------
    // Data to be preserved between callbacks.

    lateinit var A: BigUnsigned
    lateinit var M1: BigUnsigned
    lateinit var K: BigUnsigned
    var challenge_opcode = 0.b

    // ---------------------------------------------------------------------------------------------

    override fun toString()
        = "cauth(${client.username})"

    // ---------------------------------------------------------------------------------------------

    override fun state_string()
        = "Session {\n" +
        "    version: ${client.version},\n" +
        "    user: ${client.username},\n" +
        "}"

    // ---------------------------------------------------------------------------------------------

    private fun spin (serv: InetSocketAddress, auth: () -> Unit)
    {
        server = serv
        socket = Socket.open()
        CompletableFuture
            .supplyAsync { socket.connect(serv) }
            .thenRun(auth)
            .exceptionally {
                Logger.info("authentication failure: {}", this)
                Logger.debug(it)
                null
            }
    }

    // ---------------------------------------------------------------------------------------------

    fun connect (server: InetSocketAddress) {
        spin(server, this::connect)
    }

    // ---------------------------------------------------------------------------------------------

    fun reconnect (server: InetSocketAddress) {
        spin(server, this::reconnect)
    }

    // ---------------------------------------------------------------------------------------------
}
