package algalon.logon.realm

/**
 * - Each realm is one of these types.
 * - These values appear on the second column of the `Cfg_Configs.dbc` client file.
 * - The effect or other values (or whether they are used) is unknown.
 */
enum class RealmType (val value: Byte)
{
    /** Normal Realm (no PvP in neutral zones) */
    Normal  (0),

    /** PvP Realm (PvP in neutral zones) */
    PvP     (1),

    /** RP Normal Realm */
    RP      (6),

    /** RP PvP Realm */
    RP_PvP  (8),
}