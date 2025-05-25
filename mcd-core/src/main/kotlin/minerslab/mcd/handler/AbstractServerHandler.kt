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
        open var commandLine = "cmd /c run.bat"
        open var inputCharset: String = Charsets.UTF_8.name
        open var outputCharset: String = Charsets.UTF_8.name
        open var rconCharset: String = Charsets.UTF_8.name
        open var rcon: RconConfig = RconConfig()

        @Serializable
        open class RconConfig {
            open var enabled: Boolean = true
            open var port: Int = 25575
            open var password: String = "123456"
            open var host: String = "localhost"
        }

    }

    protected lateinit var serverProcess: Process
    private lateinit var config: T
    protected lateinit var outputThread: Thread
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
        outputThread = createOutputThread()
        inputThread.start()
        outputThread.start()
        getProcess().onExit()
            .thenRun(mcDaemon::stop)
    }

    protected open fun createInputThread(writeLock: Any = Any()): Thread = Thread {
        while (true) {
            val line = readlnOrNull() ?: break
            try {
                synchronized(writeLock) {
                    tickInput(line)
                }
            } catch (e: IOException) {
                logger.error("Failed to write to server process", e)
                break
            }
        }
    }

    protected open fun createOutputThread(): Thread = Thread {
        val process = getProcess()
        val reader = process.inputStream.bufferedReader(Charset.forName(config.outputCharset))
        while (process.isAlive) {
            val line = reader.readLine() ?: break
            println(line) // 重定向到控制台
            tickOutput(line)
        }
        reader.close()
    }

    open fun tickInput(line: String) {
        serverProcess.outputStream.write((line + "\n").toByteArray(Charset.forName(config.inputCharset)))
        serverProcess.outputStream.flush()
    }

    open fun tickOutput(line: String) {
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
        outputThread.interrupt()
        inputThread.join(1000)
        outputThread.join(1000)
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