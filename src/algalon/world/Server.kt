package algalon.world
import algalon.utils.net.AcceptSocket
import algalon.utils.net.Socket
import algalon.utils.sleep
import org.pmw.tinylog.Logger
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.CompletionHandler
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RejectedExecutionException

/**
* Accepts connection requests to the world server.
*/
class Server (val conf: Config)
{
    // ---------------------------------------------------------------------------------------------

    @Volatile private var shutdown = false
    private val handler = AcceptHandler()
    private val thread_pool = ForkJoinPool(conf.threads)
    private val async_group = AsynchronousChannelGroup.withThreadPool(thread_pool)

    // ---------------------------------------------------------------------------------------------

    inner class AcceptHandler: CompletionHandler<Socket, AcceptSocket>
    {
        override fun completed (socket: Socket, accept_socket: AcceptSocket)
        {
            try {
                if (!shutdown) {
                    thread_pool.execute { Session(this@Server, socket).receive_packet() }
                    accept_socket.accept(accept_socket, this)
                }
                else socket.close()
            }
            catch (e: RejectedExecutionException) {
                socket.close()
                if (!shutdown) {
                    Logger.error("world pool rejection")
                    Logger.error(e)
                }
            }
        }

        override fun failed (exc: Throwable, accept_socket: AcceptSocket)
        {
            Logger.error("auth loop failed")
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
     * Waits for there to be no more active auth sessions (polled at one second intervals), or
     * at most [seconds] seconds. If [seconds] is negative, waits an unbounded amount of time.
     */
    fun await_quiescence (seconds: Long = -1)
    {
        var elapsed = 0L
        while (false) {
            sleep(1000)
            if (seconds > 0 && ++ elapsed == seconds) break
        }
    }

    // ---------------------------------------------------------------------------------------------
}