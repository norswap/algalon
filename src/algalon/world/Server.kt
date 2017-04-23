package algalon.world
import algalon.settings.N_WORLD_THREADS
import algalon.settings.WORLD_SERVER_ADDR
import algalon.settings.WORLD_SHUTDOWN_TIMEOUT
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
class Server(n_threads: Int = N_WORLD_THREADS)
{
    // ---------------------------------------------------------------------------------------------

    @Volatile private var shutdown = false
    private val handler = AcceptHandler()
    private val thread_pool = ForkJoinPool(n_threads)
    private val async_group = AsynchronousChannelGroup.withThreadPool(thread_pool)

    // ---------------------------------------------------------------------------------------------

    inner class AcceptHandler: CompletionHandler<Socket, AcceptSocket>
    {
        override fun completed(socket: Socket, accept_socket: AcceptSocket)
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

        override fun failed(exc: Throwable, ignored: AcceptSocket)
        {
            Logger.error("auth loop failed")
            Logger.error(exc)
            ignored.close()
            if (!shutdown) start()
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun start()
    {
        val socket = AcceptSocket.open(async_group)
        socket.bind(WORLD_SERVER_ADDR)
        socket.accept(socket, handler)
    }

    // ---------------------------------------------------------------------------------------------

    fun shutdown()
    {
        shutdown = true
        thread_pool.shutdown()
        // better than `thread_pool.awaitTermination`
        await_quiescence(WORLD_SHUTDOWN_TIMEOUT)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Waits for there to be no more active auth sessions (polled at one second intervals), or
     * at most [seconds] seconds. If [seconds] is negative, waits an unbounded amount of time.
     */
    fun await_quiescence (seconds: Long = -1)
    {
        var elapsed = 0L
        // TODO
        while (false) {
            sleep(1000)
            //if (count.get() == 0) break
            if (++ elapsed == seconds) break
        }
    }

    // ---------------------------------------------------------------------------------------------
}