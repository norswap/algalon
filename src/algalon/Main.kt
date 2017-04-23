package algalon
import algalon.logon.Realm
import algalon.logon.server.LogonServer
import algalon.logon.version
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

    val realm = Realm {
        _id = 7
        _name = "Algalon"
        _ip = "127.0.0.1"
        _port = WORLD_LISTEN_PORT
        _version = version(1, 12, 1)
        //_version = version(3, 3, 5)
        _type = Realm.Type.Normal
    }

    Realm.list += realm

    logon_server.start()
    world_server.start()
    //client.start()
    wait_forever()
}