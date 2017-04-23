@file:Suppress("PackageDirectoryMismatch")
package algalon.logon.err

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_SUCCESS: Byte = 0x00

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_UNKNOWN1: Byte = 0x01
val AUTH_FAIL_UNKNOWN2: Byte = 0x02

// (va, bc) Unable to connect

// (lk) Unable to connect. Please try again later. If the problem persists, please contact
//      technical support at: <url>

// <url>: http://us.blizzard.com/support/index.xml?gameId=11&rootCategoryId=2316

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_BANNED: Byte = 0x03

// This World of Warcraft account has been closed and is no longer available for use.
// Please go to <url> for further information.

// <url>: http://www.worldofwarcraft.com/misc/banned.html

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_INVALID_CREDENTIALS1: Byte = 0x04
val AUTH_FAIL_INVALID_CREDENTIALS2: Byte = 0x05

// The information you have entered is not valid. Please check the spelling of the
// account name and password. If you need help in retrieving a lost or stolen password and
// account, see <url> for more information.

// <url> (va, bc) : www.worldofwarcraft.com
// <url> (lk)     : www.worldofwarcraft.com/loginsupport

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_ALREADY_ONLINE: Byte = 0x06

// This account is already logged into World of Warcraft. Please check the spelling and try
// again.

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_NO_PREPAID_TIME: Byte = 0x07

// (va, bc) You have used up your prepaid time for this account. Please purchase more to
// continue playing

// (lk) You have used up your prepaid time for this account. Please visit <url> to purchase
// more to continue playing.

// <url>: www.worldofwarcraft.com/account

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_TRY_LATER: Byte = 0x08

// Could not log in to World of Warcraft at this time. Please try again later.

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_VERSION_INVALID: Byte = 0x09

// Unable to validate game version. This may be caused by file corruption or the interference of
// another program. Please visit <url> for more information and possible solutions to this
// issue.

// <url> (va): www.blizzard.com/support/wow/
// <url> (bc): http://us.blizzard.com/support/article.xml?articleId=21370
// <url> (lk): http://us.blizzard.com/support/article.xml?articleId=21031

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_VERSION_UPDATE: Byte = 0x0A

// Downloading

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_UNKNOWN3: Byte = 0x0B

// Same as AUTH_FAIL_UNKNOWN1

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_SUSPENDED: Byte = 0x0C

// This World of Warcraft account has been temporarily suspended. Please go to
// <url> for further information.

// <url>:  http://www.worldofwarcraft.com/misc/banned.html

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_FAIL_UNKNOWN4: Byte = 0x0D

// Same as AUTH_FAIL_UNKNOWN1

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_SUCCESS_SURVEY: Byte = 0x0E

// This one is rather mysterious. First is displays "Success!" with to option to cancel.
// Cancelling results in "Retrieving realm list", also with the option to cancel.

// Apparently, cancelling twice does not close the socket. Meaning the next attempt to connect
// results in a hanging "Authenticating" message (because the server is not reading data
// anymore). Cancelling that one closes the socket and brings things back to normal.

/**
 * Since the server closes the server on its end after a delay, the hanging "Authenticating" is
 * automatically dismissed after a few seconds (or doesn't appear at all if the user waits a few
 * seconds before trying to reconnect).
 */

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_PARENTCONTROL: Byte = 0x0F

// (va) Access to this account has been blocked by parental controls. Your settings may be
// changed in your account preferences at <url>.

// <url>: http://www.worldofwarcraft.com

// (bc, lk) Access to this account is currently restricted by parental controls. You can change
// your control settings from your online account management. [Manage Account] [Okay]

// Clicking [Manage Account] opens www.worldofwarcraft.com/account

////////////////////////////////////////////////////////////////////////////////////////////////////

// Subsequent error codes cause "Disconnect from server" to be displayed in the Vanilla client.

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_LOCKED_ENFORCED: Byte = 0x10

// (bc, lk) You have applied a lock to your account. You can change your locked status by
// calling your account lock phone number.

////////////////////////////////////////////////////////////////////////////////////////////////////

// Subsequent error codes cause "Disconnect from server" to be displayed in the BC client.

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_TRIAL_ENDED: Byte = 0x11

// (lk) Your trial subscription has expired. Please visit <url> to upgrade your account.

// <url>: www.worldofwarcraft.com/account

////////////////////////////////////////////////////////////////////////////////////////////////////

val AUTH_FAIL_USE_BATTLENET: Byte = 0x12

// (lk) This account is now attached to a Battle.net account. Please log in with your
// Battle.net account email address (example: john.doe@blizzard.com) and password.

////////////////////////////////////////////////////////////////////////////////////////////////////

// Subsequent error codes exist for subsequent expansions (after WotLK). They cause the
// AUTH_FAIL_UNKNOWN1 message to be displayed in the WotLK client.

////////////////////////////////////////////////////////////////////////////////////////////////////

val errcodeNames = arrayOf(
    "AUTH_SUCCESS",
    "AUTH_FAIL_UNKNOWN1",
    "AUTH_FAIL_UNKNOWN2",
    "AUTH_FAIL_BANNED",
    "AUTH_FAIL_INVALID_CREDENTIALS1",
    "AUTH_FAIL_INVALID_CREDENTIALS2",
    "AUTH_FAIL_ALREADY_ONLINE",
    "AUTH_FAIL_NO_PREPAID_TIME",
    "AUTH_FAIL_TRY_LATER",
    "AUTH_FAIL_VERSION_INVALID",
    "AUTH_FAIL_VERSION_UPDATE",
    "AUTH_FAIL_UNKNOWN3",
    "AUTH_FAIL_SUSPENDED",
    "AUTH_FAIL_FAIL_UNKNOWN4",
    "AUTH_SUCCESS_SURVEY",
    "AUTH_FAIL_PARENTCONTROL",
    "AUTH_FAIL_LOCKED_ENFORCED",
    "AUTH_FAIL_TRIAL_ENDED",
    "AUTH_FAIL_USE_BATTLENET"
)

// -------------------------------------------------------------------------------------------------

fun errcode_name(i: Int): String
{
    if (i < errcodeNames.size)
        return errcodeNames[i]
    else
        return "UNKNWON_ERRCODE"
}

////////////////////////////////////////////////////////////////////////////////////////////////////