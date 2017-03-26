package algalon.world
import algalon.utils.HasStateString
import algalon.utils.net.Socket
import algalon.utils.net.SocketHook

class Session (val server: WorldServer, val sock: Socket): HasStateString, SocketHook
{
    override fun state_string(): String {
        TODO()
    }

    override fun close_hook() {
        TODO()
    }
}