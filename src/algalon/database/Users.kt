package algalon.database
import algalon.logon.crypto.private_key
import algalon.logon.crypto.user_verifier
import algalon.logon.realm.Realm
import algalon.utils.BigUnsigned
import algalon.utils.uppercase_ascii
import algalon.utils.RANDOM
import algalon.utils.byte_array_from_hex_string
import algalon.utils.utf8
import java.sql.ResultSet

// =================================================================================================

class Account(
    val name: String,
    val mail: String,
    val pwd_salt: ByteArray,
    val pwd_verifier: BigUnsigned,
    var session_key: BigUnsigned? = null
)

// =================================================================================================

object Accounts
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Create a new account (in-memory) with the given name, mail and password.
     * Note the password is not saved and so can't retrieve (as it should be).
     */
    fun create (name: String, mail: String, password: String): Account
    {
        val salt = ByteArray(32)
        val name_upper = name.uppercase_ascii()
        val pass_upper = password.uppercase_ascii()
        RANDOM.nextBytes(salt)
        val login_hash = private_key(name_upper.utf8, pass_upper.utf8, salt)
        return Account(name_upper, mail, salt, user_verifier(login_hash))
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Construct an account from a SQL table row.
     */
    fun from_sql (r: ResultSet) = Account(
        name = r.getString(1),
        mail = r.getString(2),
        pwd_salt = byte_array_from_hex_string(r.getString(3)),
        pwd_verifier = BigUnsigned(r.getString(4)),
        session_key = BigUnsigned(r.getString(5)))

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

object Users
{
    // ---------------------------------------------------------------------------------------------

    private val users = HashMap<String, Account>()

    // ---------------------------------------------------------------------------------------------

    fun create (username: String, password: String): Account
    {
        val salt = ByteArray(32)
        val name_upper = username.uppercase_ascii()
        val pass_upper = password.uppercase_ascii()
        RANDOM.nextBytes(salt)
        val login_hash = private_key(name_upper.utf8, pass_upper.utf8, salt)
        val user = Account(name_upper, "placeholder@mail.com", salt, user_verifier(login_hash))
        // NOTE: insert user in DB here
        users[name_upper] = user
        return user
    }

    // ---------------------------------------------------------------------------------------------

    fun load (name_upper: String, handler: (Account?) -> Unit)
    {
        val user = users[name_upper]
        if (user != null) user.session_key = ChilledSessions.defrost(name_upper)
        handler(user)
    }

    // ---------------------------------------------------------------------------------------------

    fun with_realms (account: Account, handler: (List<Realm>, Map<Int, Int>) -> Unit)
    {
        // stubby
        val realms = Realm.list
        handler(realms, realms.associate { it.id to 0 })
    }
}

// =================================================================================================