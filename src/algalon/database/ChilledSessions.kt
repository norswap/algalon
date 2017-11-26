package algalon.database
import algalon.utils.Clock
import algalon.utils.BigUnsigned
import org.jctools.maps.NonBlockingHashMap
import org.pmw.tinylog.Logger

// TODO delete
/** Delay in milliseconds after which to clean up the chilled sessions store. */
val CHILLED_SESSIONS_THAW_DELAY = Clock.minutes(10)

/**
 * This object handles the (purely in-memory) persistence of session keys from disconnected session.
 *
 * Every once in a while ([CHILLED_SESSIONS_THAW_DELAY]), old sessions are removed.
 */
object ChilledSessions
{
    // ---------------------------------------------------------------------------------------------

    private class Session (val K: BigUnsigned, val time: Long = Clock.now)

    // ---------------------------------------------------------------------------------------------

    private val map = NonBlockingHashMap<String, Session>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Stores the session key [K] for user [username].
     */
    fun chill (username: String, K: BigUnsigned) {
        map.put(username, Session(K))
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the session key associated to [username], or null there isn't one.
     */
    fun defrost (username: String): BigUnsigned?
        = map.remove(username)?.K

    // ---------------------------------------------------------------------------------------------

    /**
     * Remove all chilled sessions that are more than 10 minutes older than [now].
     */
    fun thaw() {
        Logger.info("thawing")
        val now = Clock.now
        map.forEach { k, v ->
            if (now - v.time > CHILLED_SESSIONS_THAW_DELAY)
                map.remove(k)
        }
    }

    // ---------------------------------------------------------------------------------------------
}