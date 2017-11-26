package algalon.database
import algalon.logon.server.Config
import algalon.utils.hex_string
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Manages connections to the login database.
 *
 * The C3P0 library takes care of most of the hard work by enabling every session to acquire
 * its own database connection (which are kept in a pool).
 *
 * In case of a failure, database access will fail, in which case, a new connection should be
 * acquired in order to perform a retry. C3P0 will handle transient database failures on connection
 * acquisition, by attempting to reconnect for 30s. If a new connection can't be acquired that way,
 * the session should be terminated.
 */
class LogonDatabase (val config: Config)
{
    // ---------------------------------------------------------------------------------------------

    private val source = ComboPooledDataSource().apply {
        driverClass = "org.postgresql.Driver"
        jdbcUrl = "jdbc:postgresql://${config.hostname}:${config.listen_port}/logon"
        user = "norswap"
        password = "effacer"
        maxStatementsPerConnection = 4 // TODO
        maxPoolSize = 100 // TODO
        isTestConnectionOnCheckin = true
        idleConnectionTestPeriod = 30
    }

    // ---------------------------------------------------------------------------------------------

    fun connect(): Connection
        = source.connection

    // ---------------------------------------------------------------------------------------------

    private inline fun <T> DatabaseSession.query_no_retry
        (query: String, vararg params: Any, f: (ResultSet) -> T): T
    {
        return connection.prepareStatement(query).use { stmt ->
            params.forEachIndexed { i, param -> stmt.setObject(i + 1, param) }
            stmt.executeQuery().use(f)
        }
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun <T> DatabaseSession.query
        (query: String, vararg params: Any, f: (ResultSet) -> T): T
    {
        return try {
            query_no_retry(query, params) { f(it) }
        }
        catch (e: SQLException) {
            connection = connect()
            query_no_retry(query, params) { f(it) }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private inline fun <T> DatabaseSession.exec_no_retry
        (query: String, vararg params: Any, f: (PreparedStatement) -> T): T
    {
        return connection.prepareStatement(query).use { stmt ->
            params.forEachIndexed { i, param -> stmt.setObject(i + 1, param) }
            stmt.execute()
            f(stmt)
        }
    }

    // ---------------------------------------------------------------------------------------------

    // TODO no doubly code + try around second exec + what then?

    private inline fun <T> DatabaseSession.exec
        (query: String, vararg params: Any, f: (PreparedStatement) -> T): T
    {
        return try {
            exec_no_retry(query, params) { f(it) }
        }
        catch (e: SQLException) {
            connection = connect()
            exec_no_retry(query, params) { f(it) }
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun account (session: DatabaseSession, name: String): Account?
    {
        return session.query("SELECT * FROM accounts WHERE name = ?", name) {
            if (it.fetchSize == 0) null
            else Accounts.from_sql(it)
        }
    }

    // ---------------------------------------------------------------------------------------------

    // TODO what inside?
    // TODO SQLTimeoutException

    fun upsert (session: DatabaseSession, account: Account)
    {
        return session.exec("INSERT INTO accounts (name, mail, pwd_salt, pwd_verifier, session key)"
            + " VALUES (?, ?, ?, ?, ?)"
            + " ON CONFLICT (name) DO UPDATE SET"
            + " mail = excluded.mail"
            + " pwd_salt = excluded.pwd_salt"
            + " pwd_verifier = excluded.pwd_verifier"
            + " session_key = excluded.session_key",
                account.name,
                account.mail,
                account.pwd_salt.hex_string,
                account.pwd_verifier.toString(),
                account.session_key.toString())
         {

        }
    }

    // ---------------------------------------------------------------------------------------------
}