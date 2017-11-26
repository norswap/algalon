package algalon.logon.server
import algalon.database.LogonDatabase
import org.pmw.tinylog.Logger
import algalon.utils.net.*
import algalon.utils.sleep
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.CompletionHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Accepts connection requests to the logon server.
 *
 * Server instantiation should not be done concurrently.
 */
class Server (val conf: Config)
{
    // ---------------------------------------------------------------------------------------------

    companion object {
        var next_id = 0
    }

    /** Information related to an IP trying to logon. */
    class IPInfo (var timestamp: Long, var budget: Long)

    // ---------------------------------------------------------------------------------------------

    /** The ID of this logon server (in case there are multiple */
    val id = next_id++

    /** The number of running [Session]. */
    val count = AtomicInteger(0)

    /** Record connection information per IP to limit the connection rate. */
    val rate_book = ConcurrentHashMap<String, IPInfo>()

    @Volatile private var shutdown = false
    private val handler = AcceptHandler()
    private val thread_pool = ForkJoinPool(conf.threads)
    private val async_group = AsynchronousChannelGroup.withThreadPool(thread_pool)

    // ---------------------------------------------------------------------------------------------

    val database = LogonDatabase(conf)

    // ---------------------------------------------------------------------------------------------

    inner class AcceptHandler: CompletionHandler<Socket, AcceptSocket>
    {
        override fun completed (socket: Socket, accept_socket: AcceptSocket)
        {
            try {
                if (!shutdown) {
                    thread_pool.execute {
                        val session = Session(this@Server, socket)
                        session.trace = conf.trace
                        session.receive_packet()
                    }
                    accept_socket.accept(accept_socket, this)
                }
                else socket.close()
            }
            catch (e: RejectedExecutionException) {
                socket.close()
                if (!shutdown) {
                    Logger.error("logon pool rejection")
                    Logger.error(e)
                }
            }
        }

        override fun failed (exc: Throwable, accept_socket: AcceptSocket)
        {
            Logger.error("logon loop failed")
            Logger.error(exc)
            accept_socket.close()
            if (!shutdown) start()
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun start()
    {
        val socket = AcceptSocket.open(async_group)
        socket.bind(conf.address)
        socket.accept(socket, handler)
    }

    // ---------------------------------------------------------------------------------------------

    fun shutdown()
    {
        shutdown = true
        thread_pool.shutdown()
        // better than `thread_pool.awaitTermination`
        await_quiescence(conf.shutdown_timeout)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Waits for there to be no more active logon sessions (polled at one second intervals), or
     * at most [seconds] seconds. If [seconds] is negative or omitted, waits an unbounded amount of
     * time.
     */
    fun await_quiescence (seconds: Long = -1)
    {
        var elapsed = 0L
        while (true) {
            sleep(1000)
            if (count.get() == 0) break
            if (++ elapsed == seconds) break
        }
    }

    // ---------------------------------------------------------------------------------------------
}