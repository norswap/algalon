package algalon
import algalon.auth.server.AuthServer
import algalon.database.ChilledSessions
import algalon.database.Users
import algalon.settings.*
import algalon.utils.wait_forever
import org.pmw.tinylog.Configurator
import org.pmw.tinylog.Level

fun main (args: Array<String>)
{
    Configurator.currentConfig()
        .formatPattern("{date:yyyy-MM-dd HH:mm:ss} {level}\t{message}")
        .level(Level.TRACE)
        .activate()

    Clock.repeat_with_delay(CHILLED_SESSIONS_THAW_DELAY) {
        ChilledSessions.thaw()
    }

    Users.create("jack", "jack")

    val auth_server = AuthServer()
    val client = Client("jack", "jack")

    auth_server.start()
    client.start()
    wait_forever()
}