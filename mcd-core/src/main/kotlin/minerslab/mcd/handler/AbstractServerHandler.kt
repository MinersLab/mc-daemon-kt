package minerslab.mcd.handler

import io.ktor.utils.io.charsets.name
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import minerslab.mcd.McDaemon
import starry.adventure.core.event.EventBus
import java.nio.charset.Charset
import java.util.*
import kotlin.io.path.div
import kotlin.text.Charsets
import kotlin.text.appendLine

abstract class AbstractServerHandler<T : AbstractServerHandler.AbstractServerConfig> : ServerHandler<T> {

    override val eventBus: EventBus = EventBus(this::class)

    @Serializable
    open class AbstractServerConfig {
        var commandLine = "cmd /c run.bat"
        var inputCharset: String = Charsets.UTF_8.name
        var outputCharset: String = Charsets.UTF_8.name
    }

    protected lateinit var serverProcess: Process
    private lateinit var config: T
    protected lateinit var daemonThread: Thread
    protected lateinit var inputThread: Thread
    private lateinit var mcDaemon: McDaemon

    override fun getConfig() = config
    override fun getProcess() = serverProcess
    override fun getDaemon() = mcDaemon

    override fun initialize(config: T, mcDaemon: McDaemon) {
        this.config = config
        this.mcDaemon = mcDaemon
        val tokenizer = StringTokenizer(config.commandLine)
        val command = Array(tokenizer.countTokens()) { tokenizer.nextToken() }
        serverProcess = ProcessBuilder(*command)
            .directory((mcDaemon.path / "server").toFile())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectErrorStream(true)
            .start()
        inputThread = createInputThread()
        daemonThread = createDaemonThread()
        inputThread.start()
        daemonThread.start()
        getProcess().onExit()
            .thenRun(mcDaemon::stop)
    }

    open fun createInputThread(writeLock: Any = Any()): Thread = Thread {
        val buffer = ByteArray(1024)
        try {
            var bytesRead: Int
            while (System.`in`.read(buffer).also { bytesRead = it } != -1) {
                synchronized(writeLock) {
                    serverProcess.outputStream.write(buffer, 0, bytesRead)
                    serverProcess.outputStream.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                serverProcess.outputStream.close()
            } catch (e: IOException) { }
        }
    }

    open fun createDaemonThread(): Thread = Thread {
        val process = getProcess()
        val reader = process.inputStream.bufferedReader(Charset.forName(config.outputCharset))
        while (process.isAlive) {
            val line = reader.readLine() ?: break
            println(line)
            tickLine(line)
        }
        reader.close()
    }

    open fun tickLine(line: String) {
        if (isPlayerMessage(line)) handlePlayerMessage(line)
        else if (isServerMessage(line)) handleServerMessage(line)
    }

    override fun dispose() {
        inputThread.join()
        daemonThread.join()
        getProcess().outputWriter(Charset.forName(config.inputCharset))
            .appendLine(getStopCommand())
    }

}