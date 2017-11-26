package algalon.logon
import algalon.logon.server.handle_client_proof

/**
 * Account flags stored on the server and sent to the client in [handle_client_proof].
 */
enum class AccountFlag (val value: Int)
{
    NONE    (0x00000000), // no flags
    GM      (0x00000001), // GM account
    TRIAL   (0x00000008), // trial account
    PROPASS (0x00800000), // arena tournament
}