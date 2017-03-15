@file:Suppress("PackageDirectoryMismatch")
package algalon.auth.op
import algalon.auth.Version
import algalon.settings.MAX_USERNAME_LEN
import algalon.utils.b

////////////////////////////////////////////////////////////////////////////////////////////////////
// Opcodes

val LOGON_CHALLENGE     = 0.b
val LOGON_PROOF         = 1.b
val RECONNECT_CHALLENGE = 2.b
val RECONNECT_PROOF     = 3.b
val REALM_LIST          = 16.b

////////////////////////////////////////////////////////////////////////////////////////////////////
// Account Flags

val ACCOUNT_FLAG_GM         = 0x00000001
val ACCOUNT_FLAG_TRIAL      = 0x00000008
val ACCOUNT_FLAG_PROPASS    = 0x00800000

////////////////////////////////////////////////////////////////////////////////////////////////////
// Packet Lengths

val CLOGON_CHALLENGE_FIX_LENGTH         = 34
val CLOGON_CHALLENGE_MAX_LENGTH         = CLOGON_CHALLENGE_FIX_LENGTH + MAX_USERNAME_LEN

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
val CRECONNECT_CHALLENGE_MAX_LENGTH     = CLOGON_CHALLENGE_MAX_LENGTH

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

////////////////////////////////////////////////////////////////////////////////////////////////////