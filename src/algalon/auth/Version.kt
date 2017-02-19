package algalon.auth

fun version (major: Int, minor: Int, bugfix: Int, build: Int): Version
{
    // TODO fancy dispatch to pre-defined objects
    return Version(major, minor, bugfix, build)
}

open class Version (
    val major   : Int,
    val minor   : Int,
    val bugfix  : Int,
    val build   : Int)
{
    override fun toString() = "$major.$minor.$bugfix-$build"
}

class InvalidVersion (a: Int, b: Int, c: Int, d: Int): Version(a, b, c, d)
