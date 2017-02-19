package algalon.utils
import java.util.Arrays

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
 * Pretty-format a byte array as a big hex number for printing.
 */
val ByteArray.hex_string: String
    get() = map { String.format("%02X", it) }.joinToString("")

// -------------------------------------------------------------------------------------------------