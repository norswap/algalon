@file:Suppress("PackageDirectoryMismatch")
package algalon.settings
import java.net.InetSocketAddress

// -------------------------------------------------------------------------------------------------

/** Minimum username length. */
val MIN_USERNAME_LEN = 4

/**
 * Maximum username length.
 *
 * In Vanilla, the client only allows for the input of 16 characters.
 */
val MAX_USERNAME_LEN = 16

// -------------------------------------------------------------------------------------------------

/** Timeout in seconds for net read operations. */
val NET_READ_TIMEOUT = 10L

/** Timeout in seconds for net write operations. */
val NET_WRITE_TIMEOUT = 10L

/** Timeout in seconds for server shutdown. */
val AUTH_SHUTDOWN_TIMEOUT = 20L

// -------------------------------------------------------------------------------------------------

/** Auth server TCP port. */
val AUTH_LISTEN_PORT = 3724

/** Number of threads to use in the auth server. */
val N_AUTH_THREADS = 2

/** Interface on which the auth server should listen for incoming connection. */
val AUTH_SERVER_ADDR = InetSocketAddress("localhost", AUTH_LISTEN_PORT)

// -------------------------------------------------------------------------------------------------

/** Trace the authentication system. */
val TRACE_AUTH = true

// -------------------------------------------------------------------------------------------------