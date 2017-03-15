package algalon
import java.util.Date
import java.util.Timer
import java.util.TimerTask

/**
 * Use this object to schedule an execution in the future, or set up a repeating task.
 */
object Clock {

    // ---------------------------------------------------------------------------------------------

    // Use a timer for now.
    // TODO: change to ScheduledThreadPoolExecutor
    // TODO: handle exceptions in scheduled threads
    private val timer = Timer()

    // ---------------------------------------------------------------------------------------------

    private class ClockTask (val task: () -> Unit): TimerTask() {
        override fun run() = task()
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the epoch (time since 1970 in milliseconds).
     */
    fun now(): Long
        = System.currentTimeMillis()

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the number of milliseconds in [n] minutes.
     */
    fun minutes (n: Long): Long
        = n * 60 * 1000

    // ---------------------------------------------------------------------------------------------

    /**
     * Schedule [task] to be run at **fixed-delay** interval of [delay].
     * Runs as soon as possible if [now], otherwise waits a full period.
     */
    fun repeat_with_delay (delay: Long, now: Boolean = false, task: () -> Unit)
    {
        val date = if (now) Date() else Date(System.currentTimeMillis() + delay)
        timer.schedule(ClockTask(task), date, delay)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Schedule [task] to be run at **fixed-rate** interval of [period].
     * Runs as soon as possible if [now], otherwise waits a full period.
     */
    fun repeat_with_period (period: Long, now: Boolean = false, task: () -> Unit)
    {
        val date = if (now) Date() else Date(System.currentTimeMillis() + period)
        timer.scheduleAtFixedRate(ClockTask(task), date, period)
    }

    // ---------------------------------------------------------------------------------------------
}