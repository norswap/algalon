package algalon.utils
import java.util.Arrays
import java.util.Random

// -------------------------------------------------------------------------------------------------

/**
 * Xor two bytes array together, byte per byte.
 */
infix fun ByteArray.xor (other: ByteArray): ByteArray
{
    assert(size == other.size)
    val out = ByteArray(size)
    for (i in indices) out[i] = (this[i].i xor other[i].i).b
    return out
}

// -------------------------------------------------------------------------------------------------

/**
 * Pretty-format a byte array for printing.
 */
val ByteArray.string: String
    get() = Arrays.toString(this)

// -------------------------------------------------------------------------------------------------

/**
 * Pretty-format a byte array as an hex number for printing.
 */
val ByteArray.hex_string: String
    get() = map { String.format("%02X", it) }.joinToString("")

// -------------------------------------------------------------------------------------------------

/**
 * Creates a byte array from a hex string description.
 */
fun byte_array_from_hex_string (str: String): ByteArray
{
    val len = str.length
    val data = ByteArray(len / 2)
    for (i in 0 until len step 2) {
        data[i/2] = ((Character.digit(str[i], 16) shl 4) + Character.digit(str[i + 1], 16)).b
    }
    return data
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns an array of [n] random bytes.
 */
fun Random.bytes(n: Int): ByteArray
{
    val random = ByteArray(n)
    nextBytes(random)
    return random
}

// -------------------------------------------------------------------------------------------------