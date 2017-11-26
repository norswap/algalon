package algalon.logon

/**
 * Represents a version of the game client.
 */
data class Version private constructor (
    val major   : Int,
    val minor   : Int,
    val bugfix  : Int,
    val mod     : String,
    val build   : Int)
{
    companion object
    {
        // -----------------------------------------------------------------------------------------

        /**
         * A list of known game versions for v1 to v4.
         *
         * Used to ensure de-duplication of version objects.
         */
        val known = listOf(
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

        // -----------------------------------------------------------------------------------------

        /**
         * Maps build number to version for known versions.
         */
        private val map = known.associateBy { it.build }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns an appropriate [Version] object given the version numbers.
         */
        operator fun invoke (major: Int, minor: Int, bugfix: Int, build: Int)
            = map[build] ?: Version(major, minor, bugfix, "", build)

        // -----------------------------------------------------------------------------------------

        /** "Standard" vanilla version. */
        val VA = Version(1, 12, 1, 5875)

        /** "Standard" BC version. */
        val BC = Version(2, 4, 3, 8606)

        /** "Standard" WotLK version. */
        val LK = Version(3, 3, 5, 12340)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A display string without the build number.
     */
    val short_string = "$major.$minor.$bugfix$mod"

    // ---------------------------------------------------------------------------------------------

    /**
     * A display string with the build number.
     */
    val long_string = "$short_string-$build"

    // ---------------------------------------------------------------------------------------------

    override fun toString() = long_string

    // ---------------------------------------------------------------------------------------------
}