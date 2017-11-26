package algalon.logon.realm
import algalon.database.Account
import algalon.logon.Version

class RealmOld private constructor ()
{
    // ---------------------------------------------------------------------------------------------

    companion object {
        var list = emptyList<RealmOld>()
    }

    // ---------------------------------------------------------------------------------------------

    constructor (init: RealmOld.() -> Unit): this() {
        init()
    }

    // ---------------------------------------------------------------------------------------------

    /** Unique realm ID. */
    val id get() = _id
    var _id = 0

    // ---------------------------------------------------------------------------------------------

    /** Displayed name of the realm. */
    val name get() = _name
    lateinit var _name: String

    // ---------------------------------------------------------------------------------------------

    /** The IP address of this realm's server. */
    val ip get() = _ip
    lateinit var _ip: String

    // ---------------------------------------------------------------------------------------------

    /** The port of this realm's server. */
    val port get() = _port
    var _port = 0

    // ---------------------------------------------------------------------------------------------

    /**
     * Client version this realm is intended for.
     * Might change this in the future to allow multi-version realms.
     */
    val version get() = _version
    lateinit var _version: Version

    // ---------------------------------------------------------------------------------------------

    /** See [RealmOld.RealmFlag] */
    val flags get() = _flags
    var _flags: List<RealmFlag> = emptyList()

    // ---------------------------------------------------------------------------------------------

    val flags_value = -1
        get() = if (field >= 0) field
        else flags.fold(0) { sum, it -> sum + it.value }.also { field = it }

    // ---------------------------------------------------------------------------------------------

    /**
     * - Represent the server's time zone (these are values out of an enum).
     * - 0 for cross-realms, we hard-code this value here.
     * - Could be used to exclude some localized clients (see Packets.txt).
     */
    val timezone = 0

    // ---------------------------------------------------------------------------------------------

    /** Normal, PvP, ... */
    val type get() = _type
    lateinit var _type: RealmType

    // ---------------------------------------------------------------------------------------------

    // TODO implement
    var population_level: Float = 0f

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the realm accepts connections from a client with the given version.
     */
    fun accepts (version: Version): Boolean {
        return this.version == version
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the realm appears locked to the given user (v2+).
     */
    fun accepts (account: Account): Boolean {
        return true
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Name to display to the client in the realm list.
     */
    fun display_name (specify_build: Boolean): String
    {
        if (specify_build)
            return "$name (${version.short_string})"
        else
            return name
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * "<ip>:<port>"
     */
    val ip_string get() = _ip_string!!
    val _ip_string: String? = null
        get() = field ?: "$ip:$port".also { field = it }

    // ---------------------------------------------------------------------------------------------
}