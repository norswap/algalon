package algalon.world
import algalon.utils.net.Socket
import algalon.utils.net.SocketHook

class Session (val server: Server, val sock: Socket): SocketHook
{
    val log_header: String = TODO()

    override fun toString(): String {
        TODO()
    }

    override fun close_hook() {
        TODO()
    }
}