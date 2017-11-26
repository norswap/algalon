package algalon
import algalon.client.Client
import algalon.database.CHILLED_SESSIONS_THAW_DELAY
import algalon.logon.server.Server as LogonServer
import algalon.logon.server.Config as LogonConf
import algalon.database.ChilledSessions
import algalon.database.Users
import algalon.logon.Version
import algalon.logon.realm.Realm
import algalon.logon.realm.RealmPopulation
import algalon.logon.realm.RealmType
import algalon.utils.Clock
import algalon.utils.wait_forever
import algalon.world.Server as WorldServer
import algalon.world.Config as WorldConf
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

    val logon_conf = LogonConf { trace = true }
    val world_conf = WorldConf {}
    val logon_server = LogonServer(logon_conf)
    val world_server = WorldServer(world_conf)

    val client = Client {
        it.username("jack")
        it.password("jack")
        it.server = logon_conf.address
        it.ip = byteArrayOf(1, 2, 3, 4)
        it.version = Version.VA
    }

    val realm = Realm {
        it.id = 0
        it.name = "Algalon"
        it.ip = "127.0.0.1"
        it.port = logon_conf.listen_port
        it.version = { it == Version.VA }
        it.type = RealmType.Normal
        it.population = RealmPopulation.LOW
    }

    Realm.list += realm

    logon_server.start()
    world_server.start()
    // client.start_trace(true)
    wait_forever()
}