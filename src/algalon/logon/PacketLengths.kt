@file:Suppress("PackageDirectoryMismatch")
package algalon.logon.lengths
import algalon.logon.Version

// -------------------------------------------------------------------------------------------------

// Constants that define the lengths of logon packets or logon packet parts.

// -------------------------------------------------------------------------------------------------

val CLOGON_CHALLENGE_FIX_LENGTH         = 34

// -------------------------------------------------------------------------------------------------

val SLOGON_CHALLENGE_MIN_LENGTH         = 3
val SLOGON_CHALLENGE_REM_LENGTH         = 116
val SLOGON_CHALLENGE_MAX_LENGTH         = 119 // ignores security bytes

// -------------------------------------------------------------------------------------------------

val CLOGON_PROOF_LENGTH                 = 75

// -------------------------------------------------------------------------------------------------

val SLOGON_PROOF_MIN_LENGTH             = 2
val SLOGON_PROOF_REM_LENGTH             = 24
val SLOGON_PROOF_MAX_LENGTH             = 26

// -------------------------------------------------------------------------------------------------

val CRECONNECT_CHALLENGE_FIX_LENGTH     = CLOGON_CHALLENGE_FIX_LENGTH

// -------------------------------------------------------------------------------------------------

val SRECONNECT_CHALLENGE_MIN_LENGTH     = 2
val SRECONNECT_CHALLENGE_REM_LENGTH     = 32
val SRECONNECT_CHALLENGE_MAX_LENGTH     = 34

// -------------------------------------------------------------------------------------------------

val CRECONNECT_PROOF_LENGTH             = 58

// -------------------------------------------------------------------------------------------------

val SRECONNECT_PROOF_LENGTH_V1           = 2
val SRECONNECT_PROOF_LENGTH_V2           = 4

val Version.SRECONNECT_PROOF_LENGTH
    get() = if (major == 1) SRECONNECT_PROOF_LENGTH_V1
            else            SRECONNECT_PROOF_LENGTH_V2

// -------------------------------------------------------------------------------------------------

val CREALM_LIST_REQUEST_LENGTH          = 5

// -------------------------------------------------------------------------------------------------

val SREALM_LIST_MAX_FIXED_LENGTH        = 22
val SREALM_LIST_MAX_RECORD_LENGTH       = 128

// -------------------------------------------------------------------------------------------------
