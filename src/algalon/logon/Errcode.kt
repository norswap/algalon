package algalon.logon

import algalon.utils.i

enum class Errcode (val b: Byte)
{
////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_SUCCESS(0x00),

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_UNKNOWN1(0x01),
    AUTH_FAIL_UNKNOWN2(0x02),

// (va, bc) Unable to connect
//
// (lk) Unable to connect. Please try again later. If the problem persists, please contact
//      technical support at: <url>
//
// <url>: http://us.blizzard.com/support/index.xml?gameId=11&rootCategoryId=2316

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_BANNED(0x03),

// This World of Warcraft account has been closed and is no longer available for use.
// Please go to <url> for further information.

// <url>: http://www.worldofwarcraft.com/misc/banned.html

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_INVALID_CREDENTIALS1(0x04),
    AUTH_FAIL_INVALID_CREDENTIALS2(0x05),

// The information you have entered is not valid. Please check the spelling of the
// account name and password. If you need help in retrieving a lost or stolen password and
// account, see <url> for more information.

// <url> (va, bc) : www.worldofwarcraft.com
// <url> (lk)     : www.worldofwarcraft.com/loginsupport

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_ALREADY_ONLINE(0x06),

// This account is already logged into World of Warcraft. Please check the spelling and try
// again.

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_NO_PREPAID_TIME(0x07),

// (va, bc) You have used up your prepaid time for this account. Please purchase more to
// continue playing

// (lk) You have used up your prepaid time for this account. Please visit <url> to purchase
// more to continue playing.

// <url>: www.worldofwarcraft.com/account

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_TRY_LATER(0x08),

// Could not log in to World of Warcraft at this time. Please try again later.

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_VERSION_INVALID(0x09),

// Unable to validate game version. This may be caused by file corruption or the interference of
// another program. Please visit <url> for more information and possible solutions to this
// issue.

// <url> (va): www.blizzard.com/support/wow/
// <url> (bc): http://us.blizzard.com/support/article.xml?articleId=21370
// <url> (lk): http://us.blizzard.com/support/article.xml?articleId=21031

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_VERSION_UPDATE(0x0A),

// Downloading

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_UNKNOWN3(0x0B),

// Same as AUTH_FAIL_UNKNOWN1

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_SUSPENDED(0x0C),

// This World of Warcraft account has been temporarily suspended. Please go to
// <url> for further information.

// <url>:  http://www.worldofwarcraft.com/misc/banned.html

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_FAIL_UNKNOWN4(0x0D),

// Same as AUTH_FAIL_UNKNOWN1

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_SUCCESS_SURVEY(0x0E),

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

    AUTH_FAIL_PARENTCONTROL(0x0F),

// (va) Access to this account has been blocked by parental controls. Your settings may be
// changed in your account preferences at <url>.

// <url>: http://www.worldofwarcraft.com

// (bc, lk) Access to this account is currently restricted by parental controls. You can change
// your control settings from your online account management. [Manage Account] [Okay]

// Clicking [Manage Account] opens www.worldofwarcraft.com/account

////////////////////////////////////////////////////////////////////////////////////////////////////

// Subsequent error codes cause "Disconnect from server" to be displayed in the Vanilla client.

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_LOCKED_ENFORCED(0x10),

// (bc, lk) You have applied a lock to your account. You can change your locked status by
// calling your account lock phone number.

////////////////////////////////////////////////////////////////////////////////////////////////////

// Subsequent error codes cause "Disconnect from server" to be displayed in the BC client.

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_TRIAL_ENDED(0x11),

// (lk) Your trial subscription has expired. Please visit <url> to upgrade your account.

// <url>: www.worldofwarcraft.com/account

////////////////////////////////////////////////////////////////////////////////////////////////////

    AUTH_FAIL_USE_BATTLENET(0x12);

// (lk) This account is now attached to a Battle.net account. Please log in with your
// Battle.net account email address (example: john.doe@blizzard.com) and password.

////////////////////////////////////////////////////////////////////////////////////////////////////

// Subsequent error codes exist for subsequent expansions (after WotLK). They cause the
// AUTH_FAIL_UNKNOWN1 message to be displayed in the WotLK client.

////////////////////////////////////////////////////////////////////////////////////////////////////

    companion object {

        /**
         * Returns the enum value associated with the given error code.
         */
        fun code (byte: Byte): Errcode
        {
            val errors = values()
            val i = byte.i
            if (i < errors.size)
                return errors[i]
            else
                throw Error("unknown errcode")
        }

        /**
         * Return the name of given error code.
         */
        fun name (byte: Byte): String
        {
            val errors = values()
            val i = byte.i
            if (i < errors.size)
                return errors[i].name
            else
                return "UNKNOWN_ERRCODE"
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////
}