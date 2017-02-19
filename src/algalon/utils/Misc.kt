package algalon.utils
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Arrays

// -------------------------------------------------------------------------------------------------

/**
 * A sample random instance, for general purpose work. Thread-safe, but only use in fixed places,
 * to avoid contention.
 */
val RANDOM = SecureRandom()

// -------------------------------------------------------------------------------------------------

/**
 * A sample SHA1 instance, to be used in initialization and at some fixed places.
 * BEWARE: NOT THREAD-SAFE
 */
val SHA1 = MessageDigest.getInstance("SHA-1")

// -------------------------------------------------------------------------------------------------

/**
 * Same as updating the digest with the parameter, then calling [MessageDigest.digest].
 */
fun MessageDigest.digest(a: ByteArray, b: ByteArray): ByteArray
{
    update(a)
    update(b)
    return digest()
}

// -------------------------------------------------------------------------------------------------

/**
 * Same as updating the digest with the parameter, then calling [MessageDigest.digest].
 */
fun MessageDigest.digest(a: ByteArray, b: ByteArray, c: ByteArray): ByteArray
{
    update(a)
    update(b)
    update(c)
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
 * `if (value != null) f(value) else null`
 */
inline fun <T: Any, R> some (value: T?, f: (T) -> R): R?
    = if (value != null) f(value) else null

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
 * Returns the current unix timestamp (aka epoch).
 */
val now: Long
    get() = System.currentTimeMillis() / 1000L

// -------------------------------------------------------------------------------------------------

/**
 * As the name implies.
 */
fun wait_forever() {
    while(true) sleep(10000)
}

// -------------------------------------------------------------------------------------------------