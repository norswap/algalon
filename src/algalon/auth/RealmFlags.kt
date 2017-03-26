package algalon.auth

// Each realm may be tagged with one or more of these flags.

// -------------------------------------------------------------------------------------------------

val REALM_FLAG_VERSION_MISMATCH = 0x01

// -------------------------------------------------------------------------------------------------

/**
 * Show the realm as offline for the client.
 */
val REALM_FLAG_OFFLINE          = 0x02

// -------------------------------------------------------------------------------------------------

/**
 * The client will be shown which build the realm is for.
 * TODO: test (version differences?)
 */
val REALM_FLAG_SPECIFY_BUILD    = 0x04

// -------------------------------------------------------------------------------------------------

val REALM_FLAG_UNK1             = 0x08

// -------------------------------------------------------------------------------------------------

val REALM_FLAG_UNK2             = 0x10

// -------------------------------------------------------------------------------------------------

val REALM_FLAG_RECOMMENDED      = 0x20

// -------------------------------------------------------------------------------------------------

val REALM_FLAG_NEW              = 0x40

// -------------------------------------------------------------------------------------------------

val REALM_FLAG_FULL             = 0x80

// -------------------------------------------------------------------------------------------------