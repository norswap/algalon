package algalon.utils
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Random

/**
 * Represents a big unsigned integer out of which a little-endian byte array can be obtained.
 *
 * The byte array representation does not include a sign bit.
 */
class BigUnsigned (val big_integer: BigInteger)
{
    // ---------------------------------------------------------------------------------------------

    init {
        assert(big_integer.signum() >= 0) // positive
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Construct the big unsigned with the given long value.
     */
    constructor (value: Long): this(BigInteger.valueOf(value))

    // ---------------------------------------------------------------------------------------------

    /**
     * Construct the big unsigned from a string representation a big endian hex number.
     */
    constructor (byte_string: String): this(BigInteger(byte_string, 16))

    // ---------------------------------------------------------------------------------------------

    /**
     * Construct the big unsigned from little-endian byte array.
     * The byte array may have trailing zero bytes.
     */
    constructor (bytes: ByteArray): this(BigInteger(bytes.big_endian())) {
        if (representation_bytes() == bytes.size) _bytes = bytes
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Construct a big unsigned with [bytes] random bytes.
     */
    constructor (random: Random, bytes: Int): this(BigInteger(bytes * 8, random))

    // ---------------------------------------------------------------------------------------------

    companion object
    {
        /**
         * Reverse and prepend a 0 (in order to force the positive sign).
         */
        private fun ByteArray.big_endian(): ByteArray
        {
            val reversed = ByteArray(size + 1)
            for (i in indices) reversed[size - i] = this[i]
            return reversed
        }
    }

    // ---------------------------------------------------------------------------------------------

    private var _bytes: ByteArray? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * A little-endian unsigned representation of this number, as a byte array.
     *
     * The returned array is as small as possible and at least [min] bytes long,
     * padded with zeroes at the end if necessary.
     */
    val bytes: ByteArray
        get() {
            _bytes?.let { return it }
            val computed = bytes()
            _bytes = computed
            return computed
        }

    // ---------------------------------------------------------------------------------------------

    /**
     * Minimum number of bytes required to represent this number.
     */
    private fun representation_bytes(): Int
        = (big_integer.bitLength() + 7) / 8 // round up

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a little-endian unsigned representation of this number, as a byte array of
     * at least [min] bytes. If necessary, 0 padding will be added at the end to reach the
     * minimum size.
     */
    fun bytes (min: Int = (representation_bytes())): ByteArray
    {
        _bytes?.let { if (it.size >= min) return it }

        // This two's complement representation has its bytes ordered big-endian.
        // The spec guarantees at least a prefix zero bit for positive numbers.
        val bigint_bytes = big_integer.toByteArray()

        // If the magnitude size in bits is a multiple of 8, an extra prefix byte is added for the
        // zero sign bit. We must discard that byte.
        val deleted_byte = if (big_integer.bitLength() % 8 == 0) 1 else 0

        // Number of bytes to copy.
        val nbytes = bigint_bytes.size - deleted_byte

        // Size of the output array, including padding if any.
        val size = if (min > nbytes) min else nbytes

        val out = ByteArray(size)
        val last_index = bigint_bytes.size - 1

        // Copy all bytes from the big-endian representation (potentially excluding the extra
        // prefix byte), reversing their order to create a little-endian representation.
        for (i in 0..(nbytes - 1)) {
            out[i] = bigint_bytes[last_index - i]
        }

        return out
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Computes `(this ^ exp) % mod`.
     */
    fun exp_mod (exp: BigUnsigned, mod: BigUnsigned): BigUnsigned
        = BigUnsigned(big_integer.modPow(exp.big_integer, mod.big_integer))

    // ---------------------------------------------------------------------------------------------

    operator fun times (other: BigUnsigned): BigUnsigned
        = BigUnsigned(big_integer * other.big_integer)

    // ---------------------------------------------------------------------------------------------

    operator fun plus (other: BigUnsigned): BigUnsigned
        = BigUnsigned(big_integer + other.big_integer)

    // ---------------------------------------------------------------------------------------------

    /**
     * Compute `(this - other) % mod`.
     */
    fun min_mod (other: BigUnsigned, mod: BigUnsigned): BigUnsigned
    {
        val tmp = (big_integer - other.big_integer) % mod.big_integer
        if (tmp.signum() < 0)
            return BigUnsigned(mod.big_integer + tmp)
        else
            return BigUnsigned(tmp)
    }

    // ---------------------------------------------------------------------------------------------

    operator fun rem (other: BigUnsigned): BigUnsigned
        = BigUnsigned(big_integer % other.big_integer)

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the big unsigned resulting from applying the given digest over
     * the bytes of this number.
     */
    fun digest (digest: MessageDigest): BigUnsigned
        = BigUnsigned(digest.digest(bytes))

    // ---------------------------------------------------------------------------------------------

    infix fun xor (other: BigUnsigned): BigUnsigned
        = BigUnsigned(bytes xor other.bytes)

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the hex string representation of the big unsigned.
     */
    override fun toString(): String
        = bytes.hex_string

    // ---------------------------------------------------------------------------------------------

    override fun equals (other: Any?): Boolean
        = other is BigUnsigned
        && big_integer == other.big_integer

    // ---------------------------------------------------------------------------------------------

    override fun hashCode(): Int
        = big_integer.hashCode()

    // ---------------------------------------------------------------------------------------------

    val is_zero: Boolean
        get() = big_integer == BigInteger.ZERO

    // ---------------------------------------------------------------------------------------------
}