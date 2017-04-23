@file:Suppress("PackageDirectoryMismatch")
package algalon.settings
import algalon.Clock
import algalon.logon.known_versions
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

/** Timeout in seconds for auth server shutdown. */
val AUTH_SHUTDOWN_TIMEOUT = 20L

/** Timeout in seconds for world server shutdown. */
val WORLD_SHUTDOWN_TIMEOUT = 20L

// -------------------------------------------------------------------------------------------------

/** Auth server TCP port. */
val AUTH_LISTEN_PORT = 3724

/** Number of threads to use in the auth server. */
val N_AUTH_THREADS = 4

/** Interface on which the auth server should listen for incoming connection. */
val AUTH_SERVER_ADDR = InetSocketAddress("localhost", AUTH_LISTEN_PORT)

// -------------------------------------------------------------------------------------------------

/** World server TCP port. */
val WORLD_LISTEN_PORT = 8085

/** Number of threads to use in the world server. */
val N_WORLD_THREADS = 4

/** Interface on which the world server should listen for incoming connection. */
val WORLD_SERVER_ADDR = InetSocketAddress("localhost", WORLD_LISTEN_PORT)

// -------------------------------------------------------------------------------------------------

/** Trace the authentication system. */
val TRACE_AUTH = true

// -------------------------------------------------------------------------------------------------

/** Delay in milliseconds after which to clean up the chilled sessions store. */
val CHILLED_SESSIONS_THAW_DELAY = Clock.minutes(10)

// -------------------------------------------------------------------------------------------------

/** Builds accepted by the authentication server. */
val ACCEPTED_BUILDS = known_versions
//val ACCEPTED_BUILDS = known_versions.filter { it.major == 1 }

// -------------------------------------------------------------------------------------------------