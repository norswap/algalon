@file:Suppress("PackageDirectoryMismatch")
package algalon.utils.net
import algalon.settings.*
import algalon.utils.HasStateString
import algalon.utils.skip
import org.pmw.tinylog.Logger
import java.io.EOFException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.channels.InterruptedByTimeoutException
import java.util.concurrent.TimeUnit

// -------------------------------------------------------------------------------------------------

/**
 * Use this socket to accept incoming connections.
 * See [AsynchronousServerSocketChannel]
 */
typealias AcceptSocket = AsynchronousServerSocketChannel

// -------------------------------------------------------------------------------------------------

/**
 * Use this socket to transmit data.
 * See [AsynchronousSocketChannel]
 */
typealias Socket = AsynchronousSocketChannel

// -------------------------------------------------------------------------------------------------

/**
 * Attachment can implement this interface to hook some operations.
 */
interface SocketHook
{
    fun close_hook()
}

// -------------------------------------------------------------------------------------------------

private fun <A> Socket.read_best_effort (buf: ByteBuffer, attachment: A, handler: SocketHandler<A>)
    = read(buf, NET_READ_TIMEOUT, TimeUnit.SECONDS, attachment, handler)

// -------------------------------------------------------------------------------------------------

private fun <A> Socket.write_best_effort (buf: ByteBuffer, attachment: A, handler: SocketHandler<A>)
    = write(buf, NET_WRITE_TIMEOUT, TimeUnit.SECONDS, attachment, handler)

// -------------------------------------------------------------------------------------------------

private abstract class SocketHandler<A> (val socket: Socket): CompletionHandler<Int, A>
{
    override fun failed (exc: Throwable, attachment: A)
    {
        val type = when (exc) {
            is InterruptedByTimeoutException    -> "connection timeout"
            is EOFException                     -> "connection closed"
            else                                -> "connection failed"
        }

        Logger.info("$type: $attachment")
        if (attachment is HasStateString)
            Logger.debug("\n" + attachment.state_string())
        if (type == "connection failed")
            Logger.debug(exc)

        cleanup(attachment)
    }

    open fun cleanup (attachment: A)
        = socket.close(attachment)
}

// -------------------------------------------------------------------------------------------------

private class ReadHandler<A> (
    var remaining: Int,
    socket: Socket,
    val buf: ByteBuffer,
    val handler: () -> Unit)
    : SocketHandler<A>(socket)
{
    override fun completed (result: Int, attachment: A)
    {
        if (result < 0) // end of stream
            return failed(EOFException(), attachment)

        remaining -= result

        if (remaining > 0)
            socket.read_best_effort(buf, attachment, this)
        else {
            buf.limit(buf.position())
            buf.reset()
            handler()
        }
    }
}

// -------------------------------------------------------------------------------------------------

private class WriteHandler<A> (
    socket: Socket,
    val buf: ByteBuffer,
    val handler: () -> Unit)
    : SocketHandler<A>(socket)
{
    override fun completed (result: Int, attachment: A)
    {
        if (buf.remaining() > 0)
            socket.write_best_effort(buf, attachment, this)
        else {
            buf.clear()
            handler()
        }
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Reads into the buffer, at the current position, at least [n] bytes from the channel.
 *
 * - If there is any data between the position and the limit, it is recycled towards the count of [n].
 * - If necessary, calls [ByteBuffer.compact] beforehand, otherwise the position remains unchanged.
 * - The limit points past the read data.
 * - If the data is available, call [handler] synchronously, otherwise call it asynchronously
 *   when the data becomes available.
 * - On error, close the channel and log the error.
 */
fun <A> Socket.read (n: Int, buf: ByteBuffer, attachment: A, handler: () -> Unit)
{
    assert(n > 0)
    val r = buf.remaining()

    if (n <= r) {
        handler()
        return
    }

    val p = buf.position()
    val c = buf.capacity()

    if (c - p < n) {
        buf.compact() // lim = c
        buf.position(0)
    }
    else
        buf.limit(c)

    buf.mark() // 0 (compacted) or p
    buf.skip(r)

    read_best_effort(buf, attachment, ReadHandler(n - r, this, buf, handler))
}

// -------------------------------------------------------------------------------------------------

/**
 * Write to the channel until there is no data remaining in the buffer (`buf.remaining() == 0`).
 * When this occurs, call [handler] aftter calling [ByteBuffer.clear].
 * On error, close the channel and log the error.
 */
fun <A> Socket.write (buf: ByteBuffer, attachment: A, handler: () -> Unit)
{
    write_best_effort(buf, attachment, WriteHandler(this, buf, handler))
}

// -------------------------------------------------------------------------------------------------

/**
 * Calls this to terminate the socket rather than [AsynchronousSocketChannel.close].
 */
fun <A> Socket.close (attachment: A)
{
    if (attachment is SocketHook) attachment.close_hook()
    close()
}

// -------------------------------------------------------------------------------------------------

val Socket.host: String
    get() = (remoteAddress as InetSocketAddress).hostString

val Socket.port: Int
    get() = (remoteAddress as InetSocketAddress).port

// -------------------------------------------------------------------------------------------------