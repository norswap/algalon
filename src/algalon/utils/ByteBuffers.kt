package algalon.utils
import java.nio.ByteBuffer

// -------------------------------------------------------------------------------------------------

/**
 * Returns an array containing the [n] next bytes from the buffer.
 */
fun ByteBuffer.bytes (n: Int): ByteArray
{
    val result = ByteArray(n)
    get(result)
    return result
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns the next byte in the byte buffer, as an int.
 */
inline val ByteBuffer.byte: Int
    get() = get().toInt()

// -------------------------------------------------------------------------------------------------

/**
 * Returns the next byte in the byte buffer, interpreted as an unsigned number, as an int.
 */
inline val ByteBuffer.ubyte: Int
    get() = get().toInt() and 0xff

// -------------------------------------------------------------------------------------------------

/**
 * Returns the next short in the byte buffer, interpreted as an unsigned number, as an int.
 */
inline val ByteBuffer.ushort: Int
    get() = short.toInt() and 0xffff

// -------------------------------------------------------------------------------------------------

/**
 * Returns the next int in the byte buffer, interpreted as an unsigned number, as a long.
 */
inline val ByteBuffer.uint: Long
    get() = int.toLong() and 0xffffffff

// -------------------------------------------------------------------------------------------------

/**
 * Skips [n] bytes from the buffer (1 by default).
 */
fun ByteBuffer.skip (n: Int = 1)
{
    position(position() + n)
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns a [BigUnsigned] created from the next [size] bytes of the buffer.
 */
fun ByteBuffer.big_unsigned (size: Int): BigUnsigned
{
    val array = ByteArray(size)
    get(array)
    return BigUnsigned(array)
}

// -------------------------------------------------------------------------------------------------

/**
 * See [ByteBuffer.put].
 */
fun ByteBuffer.put (vararg bytes: Byte)
{
    put(bytes)
}

// -------------------------------------------------------------------------------------------------