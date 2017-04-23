package algalon.logon
import algalon.database.User

class Realm private constructor ()
{
    // ---------------------------------------------------------------------------------------------

    companion object {
        var list = emptyList<Realm>()
    }

    // ---------------------------------------------------------------------------------------------

    constructor (init: Realm.() -> Unit): this() {
        init()
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * - Each realm is one of these types.
     * - These values appear on the second column of the `Cfg_Configs.dbc` client file.
     * - The effect or other values (or whether they are used) is unknown.
     */
    enum class Type (val value: Byte) {
        Normal(0),  // Normal Realm (no PvP in neutral zones)
        PvP(1),     // PvP Realm (PvP in neutral zones)
        RP(6),      // RP Normal Realm
        RP_PvP(8)   // RP PvP Realm
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Each realm can have multiple of these flags, which produce the indicated effect.
     * The effects were only tested on the Vanilla client.
     *
     * For the population field, the priority order is:
     * OFFLINE > RECOMMENDED > NEW > FULL
     *
     * If FULL is overriden in this manner, it will not produce its warning effect.
     */
    enum class Flag (val value: Int)
    {
        // Highlights server in red. Connection still possible.
        VERSION_MISMATCH(0x01),

        // Population: Offline. Connection impossible.
        OFFLINE(0x02),

        // Report required build (extensions only).
        SPECIFY_BUILD(0x04),

        // Population: Recommended. Picks the realms from the picker if it has the correct type.
        RECOMMENDED(0x20),

        // Population: new.
        NEW(0x40),

        // Population: full. Warns if trying to pick the realm.
        FULL(0x80),
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Possible population levels: low, medium, or high ("full" is handled via a [Flag]).
     */
    enum class Population (val value: Float)
    {
        // In reality, low if < 1.0, high if > 1.0.
        // Tested on Vanilla only.
        LOW(0.0f),
        MEDIUM(1.0f),
        HIGH(2.0f)
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

    /** See [Realm.Flag] */
    val flags get() = _flags
    var _flags: List<Flag> = emptyList()

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
    lateinit var _type: Type

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
    fun accepts (user: User): Boolean {
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