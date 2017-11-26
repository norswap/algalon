package algalon.utils
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Arrays

// -------------------------------------------------------------------------------------------------

/**
 * A sample random instance, for general purpose work. Thread-safe, but only use in places
 * whose load does not scale with the number of clients (or worse, interactions) to avoid
 * contention.
 */
val RANDOM = SecureRandom()

// -------------------------------------------------------------------------------------------------

/**
 * A sample SHA1 instance, to be used in initialization and at some pre-determined places.
 * BEWARE: NOT THREAD-SAFE
 */
val SHA1 = MessageDigest.getInstance("SHA-1")!!

// -------------------------------------------------------------------------------------------------

/**
 * Update the digest with all the given arrays, then calls [MessageDigest.digest].
 */
@Suppress("NOTHING_TO_INLINE")
inline fun MessageDigest.digest (vararg arrays: ByteArray): ByteArray
{
    for (array in arrays) update(array)
    return digest()
}

// -------------------------------------------------------------------------------------------------

/**
 * Shorthand for [StringBuilder.append].
 */
operator fun StringBuilder.plusAssign (o: Any?)
{
    append(o)
}

// -------------------------------------------------------------------------------------------------

/**
 * Pretty-format an array for printing.
 */
val <T> Array<T>.string: String
    get() = Arrays.toString(this)

// -------------------------------------------------------------------------------------------------

/**
 * Attempt the sleep the requested number of milliseconds, but can be interrupted before.
 * Does not throw an exception if interrupted.
 */
fun sleep (millis: Long) {
    try { Thread.sleep(millis) }
    catch (e: InterruptedException) {}
}

// -------------------------------------------------------------------------------------------------

/**
 * As the name implies.
 */
fun wait_forever() {
    while(true) sleep(10000)
}

// -------------------------------------------------------------------------------------------------