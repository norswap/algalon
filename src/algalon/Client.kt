package algalon
import algalon.logon.version
import algalon.logon.Version
import algalon.logon.client.Session
import algalon.settings.*
import algalon.utils.reverse_array
import algalon.utils.uppercase_ascii

class Client (
    username: String,
    password: String,
    val version: Version = version(1, 12, 1),
    val game: ByteArray = "WoW".reverse_array(nul = true),
    val platform: ByteArray = "x86".reverse_array(nul = true),
    val os: ByteArray = "Win".reverse_array(nul = true),
    val lang: ByteArray = "enUS".reverse_array(),
    val timezone_bias: Int = 0,
    val ip: ByteArray = byteArrayOf(1, 2, 3, 4)
) {
    val username = username.uppercase_ascii()
    val password = password.uppercase_ascii()

    fun start() {
        Session(this).connect(AUTH_SERVER_ADDR)
    }
}
