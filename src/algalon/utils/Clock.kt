package algalon.utils
import org.pmw.tinylog.Logger
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Use this object to schedule an execution in the future, or set up a repeating task.
 */
object Clock
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Timeout in seconds for the orderly shutdown of the clock.
     */
    var shutdown_timeout = 20L

    // ---------------------------------------------------------------------------------------------

    /**
     * Number of threads on which scheduled tasks may execute concurrently.
     */
    var threads = 8

    // ---------------------------------------------------------------------------------------------

    class Executor (threads: Int): ScheduledThreadPoolExecutor(threads)
    {
        override fun afterExecute (r: Runnable?, t: Throwable?)
        {
            if (t != null)
                Logger.error("Scheduled task completed exceptionally: " + t.message)
        }
    }

    // ---------------------------------------------------------------------------------------------

    private val executor = Executor(threads)

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the unix timestamp (time since 1970) in milliseconds.
     */
    val now: Long
        get() = System.currentTimeMillis()

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the unix timestamp (time since 1970) in seconds.
     */
    val timestamp: Long
        get() = System.currentTimeMillis() / 1000L

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the number of milliseconds in [n] minutes.
     */
    fun minutes (n: Long): Long
        = n * 60 * 1000

    // ---------------------------------------------------------------------------------------------

    /**
     * Schedule [task] to be run at **fixed-delay** interval of [delay] milliseconds.
     * Runs as soon as possible if [now], otherwise waits a full period.
     */
    fun repeat_with_delay (delay: Long, now: Boolean = false, task: () -> Unit)
    {
        val initial_delay = if (now) 0 else delay
        executor.scheduleWithFixedDelay(task, initial_delay, delay, MILLISECONDS)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Schedule [task] to be run at **fixed-rate** interval of [period].
     * Runs as soon as possible if [now], otherwise waits a full period.
     */
    fun repeat_with_period (period: Long, now: Boolean = false, task: () -> Unit)
    {
        val initial_delay = if (now) 0 else period
        executor.scheduleWithFixedDelay(task, initial_delay, period, MILLISECONDS)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Shutdowns the clock and waits for all already submitted tasks to finish executing
     * (or the shutdown timeout elapses).
     */
    fun shutdown()
    {
        executor.shutdown()
        val terminated = executor.awaitTermination(shutdown_timeout * 1000L, MILLISECONDS)
        if (!terminated)
            // may not prove very effective ...
            executor.shutdownNow()
    }

    // ---------------------------------------------------------------------------------------------
}