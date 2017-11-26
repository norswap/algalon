package algalon.logon.realm

/**
 * Possible population levels: low, medium, or high ("full" is handled via a [RealmFlag]).
 */
enum class RealmPopulation (val value: Float)
{
    // In reality, low if < 1.0, high if > 1.0.
    // Tested on Vanilla only.

    LOW     (0.0f),
    MEDIUM  (1.0f),
    HIGH    (2.0f)
}