package algalon.database
import algalon.auth.Realms
import algalon.auth.crypto.private_key
import algalon.auth.crypto.user_verifier
import algalon.utils.BigUnsigned
import algalon.utils.uppercase_ascii
import algalon.utils.RANDOM
import algalon.utils.utf8

// =================================================================================================

class User (
    val username: String,
    val salt: ByteArray,
    val verifier: BigUnsigned,
    var K: BigUnsigned? = null
)

// =================================================================================================

object Users
{
    // ---------------------------------------------------------------------------------------------

    private val users = HashMap<String, User>()

    // ---------------------------------------------------------------------------------------------

    fun create (username: String, password: String): User
    {
        val salt = ByteArray(32)
        val name_upper = username.uppercase_ascii()
        val pass_upper = password.uppercase_ascii()
        RANDOM.nextBytes(salt)
        val login_hash = private_key(name_upper.utf8, pass_upper.utf8, salt)
        val user = User(name_upper, salt, user_verifier(login_hash))
        // NOTE: insert user in DB here
        users[name_upper] = user
        return user
    }

    // ---------------------------------------------------------------------------------------------

    fun load (name_upper: String, handler: (User?) -> Unit)
    {
        val user = users[name_upper]
        if (user != null) user.K = ChilledSessions.defrost(name_upper)
        handler(user)
    }

    // ---------------------------------------------------------------------------------------------

    fun get_character_counts (user: User, handler: (Map<Int, Int>) -> Unit)
    {
        // stubby
        handler(Realms.list.associate { it.id to 0 })
    }
}

// =================================================================================================