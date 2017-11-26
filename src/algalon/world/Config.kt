package algalon.world
import java.net.InetSocketAddress

/**
 * Configuration for a world server.
 */
class Config private constructor()
{
    // ---------------------------------------------------------------------------------------------

    constructor (init: Config.() -> Unit): this() { init() }

    // ---------------------------------------------------------------------------------------------

    /** Interface on which the server should listen for incoming connections. */
    var hostname = "localhost"

    // ---------------------------------------------------------------------------------------------

    /** Server TCP port. */
    var listen_port = 8085

    // ---------------------------------------------------------------------------------------------

    /**
     * Combination of [hostname] and [listen_port].
     *
     * Use [InetSocketAddress.getHostString] to get string representation.
     */
    val address: InetSocketAddress
        get() = InetSocketAddress(hostname, listen_port)

    // ---------------------------------------------------------------------------------------------

    /** Number of threads to use for the server. */
    var threads = 4

    // ---------------------------------------------------------------------------------------------

    /** Timeout in seconds for shutdown. */
    var shutdown_timeout = 20L

    // ---------------------------------------------------------------------------------------------
}