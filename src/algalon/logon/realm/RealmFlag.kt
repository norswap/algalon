package algalon.logon.realm

/**
 * Each realm can have multiple of these flags, which produce the indicated effect.
 * The effects were only tested on the Vanilla client.
 *
 * For the population field, the priority order is:
 * OFFLINE > RECOMMENDED > NEW > FULL
 *
 * If FULL is overriden in this manner, it will not produce its warning effect.
 */
enum class RealmFlag (val value: Int)
{
    /** Highlights server in red. Connection still possible. */
    VERSION_MISMATCH(0x01),

    /** Population: Offline. Connection impossible. */
    OFFLINE(0x02),

    // Report required build (v2+). We don't use that crap. */
    SPECIFY_BUILD(0x04),

    /** Population: Recommended. Picks the realms from the picker if it has the correct type. */
    RECOMMENDED(0x20),

    /** Population: New. */
    NEW(0x40),

    /** Population: Full. Warns if trying to pick the realm. */
    FULL(0x80),
}