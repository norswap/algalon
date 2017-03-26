package algalon.auth
import algalon.database.User

class Realm (

    // ---------------------------------------------------------------------------------------------

    /** Unique realm ID. */
    val id: Int,

    // ---------------------------------------------------------------------------------------------

    /** Displayed name of the realm. */
    val name: String,

    // ---------------------------------------------------------------------------------------------

    /** The IP address of this realm's server. */
    val ip: String,

    // ---------------------------------------------------------------------------------------------

    /** The port of this realm's server. */
    val port: Int,

    // ---------------------------------------------------------------------------------------------

    /**
     * Client version this realm is intended for.
     * Might change this in the future to allow multi-version realms.
     */
    val version: Version,

    // ---------------------------------------------------------------------------------------------

    /** See RealmFlags.kt */
    val flags: Int,

    // ---------------------------------------------------------------------------------------------

    /** See RealmTimezones.kt */
    val timezone: Int,

    // ---------------------------------------------------------------------------------------------

    /** Normal, PvP, ... */
    val type: Type)

{   // ---------------------------------------------------------------------------------------------

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
     * Traditionally computed as `(num players / max players) * 2`.
     * (Online players or accounts? Online player limit?)
     * TODO: implement this
     *
     * In the realm list in-game, the thresholds for low, medium, and high population are
     * 0.5, 1.0, and 2.0 respectively.
     */
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
     * Whether the realm accepts connections from the given account.
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

    val ip_string
        = "$ip:$port"

    // ---------------------------------------------------------------------------------------------
}