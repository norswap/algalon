package algalon.auth.client
import algalon.Client
import algalon.utils.BigUnsigned
import algalon.utils.HasStateString
import algalon.utils.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.security.MessageDigest

/**
 * Container for data related to an authentication attempt on the client side.
 */
class ClientSession (val client: Client, val sock: Socket): HasStateString
{
    // ---------------------------------------------------------------------------------------------

    val sha1 = MessageDigest.getInstance("SHA-1")

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

    // ---------------------------------------------------------------------------------------------

    override fun toString()
        = "cauth(${client.username})"

    // ---------------------------------------------------------------------------------------------

    override fun state_string()
        = "ClientSession {\n" +
        "    version: ${client.version},\n" +
        "    user: ${client.username},\n" +
        "}"

    // ---------------------------------------------------------------------------------------------
}
