package algalon
import algalon.auth.Version
import algalon.auth.client.ClientSession
import algalon.auth.client.authenticate
import algalon.settings.*
import algalon.utils.net.Socket
import algalon.utils.reverse_array
import algalon.utils.uppercase_ascii
import org.pmw.tinylog.Logger
import java.util.concurrent.CompletableFuture

class Client (
    username: String,
    password: String,
    val version: Version = Version(1, 12, 1, 5875),
    val game: ByteArray = "WoW".reverse_array(nul = true),
    val platform: ByteArray = "x86".reverse_array(nul = true),
    val os: ByteArray = "Win".reverse_array(nul = true),
    val lang: ByteArray = "enUS".reverse_array(),
    val timezone_bias: Int = 0,
    val ip: ByteArray = byteArrayOf(1, 2, 3, 4)
) {
    val username = username.uppercase_ascii()
    val password = password.uppercase_ascii()

    fun start()
    {
        val sock = Socket.open()
        CompletableFuture
            .supplyAsync { sock.connect(AUTH_SERVER_ADDR) }
            .thenRun { ClientSession(this, sock).authenticate() }
            .exceptionally {
                Logger.info("authentication failure: {}", this)
                Logger.debug(it)
                null
            }
    }
}
