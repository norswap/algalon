package algalon.utils

// -------------------------------------------------------------------------------------------------

/**
 * Reverses the strings and returns it as a byte array. If [nul] is supplied and true,
 * adds a terminating nul byte.
 */
fun String.reverse_array (nul: Boolean = false): ByteArray
{
    val out = ByteArray(length) { this[lastIndex - it].b }
    return if (nul) out + '\u0000'.b else out
}

// -------------------------------------------------------------------------------------------------

/**
 * Encodes the string to a byte array using UTF-8.
 */
val String.utf8: ByteArray
    get() = toByteArray(Charsets.UTF_8)

// -------------------------------------------------------------------------------------------------

/**
 * Returns the string with all ascii letters replaced by their uppercase equivalent.
 */
fun String.uppercase_ascii(): String
{
    val b = StringBuilder()
    for (c in this)
        if (c in 'a'..'z' || c in 'A'..'Z')
            b += c.toUpperCase()
        else
            b += c
    return b.toString()
}

// -------------------------------------------------------------------------------------------------