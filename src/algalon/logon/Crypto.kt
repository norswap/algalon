@file:Suppress("PackageDirectoryMismatch")
package algalon.logon.crypto
import algalon.utils.BigUnsigned
import algalon.utils.SHA1
import algalon.utils.digest
import algalon.utils.utf8
import java.security.MessageDigest

// -------------------------------------------------------------------------------------------------
// CONSTANTS

val k  = BigUnsigned(3)
val g  = BigUnsigned(7)
val N  = BigUnsigned("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7")
val Ng = (N.digest(SHA1)) xor (g.digest(SHA1))

private val COLON = ":".utf8

// -------------------------------------------------------------------------------------------------

fun M1 (sha1: MessageDigest, name_utf8: ByteArray, salt: ByteArray,
        A: BigUnsigned, B: BigUnsigned, K: BigUnsigned): BigUnsigned
{
    val name_hash = sha1.digest(name_utf8)
    sha1.update(Ng.bytes)
    sha1.update(name_hash)
    sha1.update(salt)
    sha1.update(A.bytes)
    sha1.update(B.bytes)
    sha1.update(K.bytes)
    return BigUnsigned(sha1.digest())
}

// -------------------------------------------------------------------------------------------------

fun private_key (name_utf8: ByteArray, pass_utf8: ByteArray, salt: ByteArray): BigUnsigned
{
    val tmp = SHA1.digest(name_utf8, COLON, pass_utf8)
    return BigUnsigned(SHA1.digest(salt, tmp))
}

// -------------------------------------------------------------------------------------------------

fun user_verifier (
    private_key: BigUnsigned,
    g: BigUnsigned = algalon.logon.crypto.g,
    N: BigUnsigned = algalon.logon.crypto.N)
: BigUnsigned
    = g.exp_mod(private_key, N)

// -------------------------------------------------------------------------------------------------

fun session_key_hash (session_key: BigUnsigned): BigUnsigned
{
    val bytes = session_key.bytes(32)

    val hash = ByteArray(40)
    val half = ByteArray(16)

    for (i in 0..15) half[i] = bytes[i * 2]
    val sha_even = SHA1.digest(half)

    for (i in 0..15) half[i] = bytes[i * 2 + 1]
    val sha_odd  = SHA1.digest(half)

    for (i in 0..19) {
        hash[i * 2] = sha_even[i]
        hash[i * 2 + 1] = sha_odd[i]
    }

    return BigUnsigned(hash)
}

// -------------------------------------------------------------------------------------------------