package algalon.database
import java.sql.Connection

/**
 * A client session that has an attached database connection.
 */
abstract class DatabaseSession
{
    lateinit var connection: Connection
}