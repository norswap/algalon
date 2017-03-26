package algalon
import algalon.auth.Realm
import algalon.auth.Realms
import algalon.auth.server.LogonServer
import algalon.auth.version
import algalon.database.ChilledSessions
import algalon.database.Users
import algalon.settings.*
import algalon.utils.wait_forever
import algalon.world.WorldServer
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

    val logon_server = LogonServer()
    val world_server = WorldServer()
    val client = Client("jack", "jack")
    val addr = WORLD_SERVER_ADDR.hostString
    println(addr)
    val realm = Realm(0, "Alagalon", "127.0.0.1", WORLD_LISTEN_PORT, version(1, 12, 1), 0, 0, Realm.Type.Normal)
    Realms.list.add(realm)

    logon_server.start()
    world_server.start()
    client.start()
    wait_forever()
}