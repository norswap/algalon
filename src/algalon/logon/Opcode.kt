package algalon.logon
import algalon.utils.b

/**
 * Opcodes for packets sent to/from the logon server.
 */
enum class Opcode (val b: Byte)
{
    LOGON_CHALLENGE     ( 0.b),
    LOGON_PROOF         ( 1.b),
    RECONNECT_CHALLENGE ( 2.b),
    RECONNECT_PROOF     ( 3.b),
    REALM_LIST          (16.b),
}