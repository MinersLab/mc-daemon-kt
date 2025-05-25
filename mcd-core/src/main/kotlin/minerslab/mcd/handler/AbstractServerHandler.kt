package minerslab.mcd.handler

import io.ktor.utils.io.charsets.name
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import minerslab.mcd.McDaemon
import org.glavo.rcon.Rcon
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starry.adventure.core.event.EventBus
import java.nio.charset.Charset
import java.util.*
import kotlin.io.path.div
import kotlin.text.Charsets

abstract class AbstractServerHandler<T : AbstractServerHandler.AbstractServerConfig> : ServerHandler<T> {

    override val logger: Logger = LoggerFactory.getLogger(this::class.java)
    override val eventBus: EventBus = EventBus(this::class)

    @Serializable
    open class AbstractServerConfig {
        var commandLine = "cmd /c run.bat"
        var inputCharset: String = Charsets.UTF_8.name
        var outputCharset: String = Charsets.UTF_8.name
        var rconCharset: String = Charsets.UTF_8.name
        var rcon: RconConfig = RconConfig()

        @Serializable
        class RconConfig {
            var enabled: Boolean = true
            var port: Int = 25575
            var password: String = "123456"
            var host: String = "localhost"
        }

    }

    protected lateinit var serverProcess: Process
    private lateinit var config: T
    protected lateinit var daemonThread: Thread
    protected lateinit var inputThread: Thread
    private lateinit var mcDaemon: McDaemon
    private var rcon: Rcon? = null

    override fun getConfig() = config
    override fun getProcess() = serverProcess
    override fun getDaemon() = mcDaemon
    override fun getRcon() = rcon

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

    protected open fun createInputThread(writeLock: Any = Any()): Thread = Thread {
        val buffer = ByteArray(1024)
        try {
            var bytesRead: Int
            while (System.`in`.read(buffer).also { bytesRead = it } != -1 && getProcess().isAlive) {
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

    protected open fun createDaemonThread(): Thread = Thread {
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
        if (isServerMessage(line)) handleServerMessage(line)
        else if (isPlayerMessage(line)) handlePlayerMessage(line)
        else if (config.rcon.enabled && rcon == null && isRconStarted(line)) {
            rcon = Rcon(config.rcon.host, config.rcon.port, config.rcon.password)
            rcon?.charset = Charset.forName(config.rconCharset)
            logger.info("Rcon connected at ${config.rcon.host}:${config.rcon.port}!")
        }
    }

    override fun dispose() {
        logger.info("Stopping")
        runCatching { rcon?.close() }
        inputThread.interrupt()
        daemonThread.interrupt()
        logger.info("Stopped")
    }

    override fun command(command: String) {
        val rcon = getRcon()
        if (rcon != null) {
            rcon.command(command)
        } else {
            val writer = getProcess().outputWriter(Charset.forName(getConfig().inputCharset))
            writer.write(getCommandHelper().processCommand(command) + "\n")
            writer.flush()
        }
    }

}