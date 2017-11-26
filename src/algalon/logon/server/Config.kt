package algalon.logon.server
import algalon.logon.Version
import java.net.InetSocketAddress

/**
 * Configuration for an logon server.
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
    var listen_port = 3724

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

    /** Timeout in milliseconds for server writes. */
    var write_timeout = 10_000L

    // ---------------------------------------------------------------------------------------------

    /** Timeout in milliseconds for server reads. */
    var read_timeout = 10_000L

    // ---------------------------------------------------------------------------------------------

    /** Timeout in seconds for shutdown. */
    var shutdown_timeout = 20L

    // ---------------------------------------------------------------------------------------------

    /** Minimum username length. */
    val min_username_len = 4

    // ---------------------------------------------------------------------------------------------

    /**
     * Maximum username length.
     *
     * The vanilla client only allows for the input of 16 characters.
     */
    var max_username_length = 16

    // ---------------------------------------------------------------------------------------------

    /** Builds accepted by the server. */
    var accepted_builds = Version.known

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the execution of the logon server should be traced or not.
     * This can be toggled on and off during execution.
     */
    var trace = false

    // ---------------------------------------------------------------------------------------------
}