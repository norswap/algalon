package algalon.auth
import algalon.settings.ACCEPTED_BUILDS

// =================================================================================================

/**
 * A list of known game versions.
 */
val known_versions = listOf(
    Version(4,  3, 4,  "", 15595),
    Version(4,  2, 2,  "", 14545),
    Version(4,  0, 6, "a", 13623),
    Version(3,  3, 5, "a", 13930), // 3.3.5a China Mainland Build
    Version(3,  3, 5, "a", 12340),
    Version(3,  3, 3, "a", 11723),
    Version(3,  3, 2,  "", 11403),
    Version(3,  3, 0, "a", 11159),
    Version(3,  2, 2, "a", 10505),
    Version(3,  1, 3,  "", 9947),
    Version(2,  4, 3,  "", 8606),
    Version(1, 12, 3,  "", 6141),
    Version(1, 12, 2,  "", 6005),
    Version(1, 12, 1,  "", 5875))

// =================================================================================================

/**
 * Maps build number to version for known versions.
 */
internal val version_map = known_versions.associateBy { it.build }

// =================================================================================================

/**
 * Returns an appropriate [Version] object given the version numbers, or an [UnknownVersion]
 * if the version isn't known.
 */
fun version (major: Int, minor: Int, bugfix: Int, build: Int): Version
    =  version_map[build] ?: UnknownVersion(major, minor, bugfix, build)

// =================================================================================================

/**
 * Returns an appropriate [Version] object given the version numbers.
 * Throws an exception if such a version isn't known (meant for internal use, do not
 * call this with data supplied by a client).
 */
fun version (major: Int, minor: Int, bugfix: Int): Version
    =  version_map.values.first { it.major == major && it.minor == minor && it.bugfix == bugfix }

// =================================================================================================

/**
 * Represents a version of the game client.
 */
open class Version (
    val major   : Int,
    val minor   : Int,
    val bugfix  : Int,
    val mod     : String,
    val build   : Int)
{
    /**
     * Whether the authentication server accepts connections from this game version.
     */
    open val accepted by lazy { ACCEPTED_BUILDS.contains(this) }

    /**
     * A display string without the build number.
     */
    val short_string = "$major.$minor.$bugfix$mod"

    /**
     * A display string with the build number.
     */
    val long_string = "$short_string-$build"

    override fun toString() = long_string
}

// =================================================================================================

/**
 * A client version that we do not know.
 */
class UnknownVersion (a: Int, b: Int, c: Int, d: Int): Version(a, b, c, "", d)
{
    override val accepted = false
}

// =================================================================================================