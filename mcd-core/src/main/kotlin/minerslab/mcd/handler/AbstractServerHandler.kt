package minerslab.mcd.handler

import kotlinx.io.IOException
import minerslab.mcd.mcDaemon
import org.glavo.rcon.Rcon
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starry.adventure.event.EventBus
import java.nio.charset.Charset
import java.util.*
import kotlin.io.path.div

abstract class AbstractServerHandler<T : AbstractServerConfig> : ServerHandler<T> {

    override val logger: Logger = LoggerFactory.getLogger(this::class.java)
    override val eventBus: EventBus = EventBus(this::class)

    protected lateinit var serverProcess: Process
    private lateinit var configDelegate: T
    protected lateinit var outputThread: Thread
    protected lateinit var inputThread: Thread
    var rcon: Rcon? = null

    override val config
        get() = configDelegate
    override val process
        get() = serverProcess

    override fun initialize(config: T) {
        configDelegate = config
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
        process.onExit()
            .thenRun(mcDaemon::stop)
    }

    protected open fun createInputThread(): Thread = Thread {
        while (true) {
            val line = System.`in`.bufferedReader(Charset.forName(config.inputCharset)).readLine() ?: break
            try {
                tickInput(line)
            } catch (e: IOException) {
                logger.error("Failed to write to server process", e)
                break
            }
        }
    }

    protected open fun createOutputThread(): Thread = Thread {
        val reader = process.inputStream.bufferedReader(Charset.forName(config.outputCharset))
        while (process.isAlive) {
            val line = reader.readLine() ?: break
            output(line) // 重定向到控制台
            tickOutput(line)
        }
        reader.close()
    }

    open fun startRcon() {
        rcon = Rcon(config.rcon.host, config.rcon.port, config.rcon.password)
        rcon?.charset = Charset.forName(config.rconCharset)
        logger.info("Rcon connected at ${config.rcon.host}:${config.rcon.port}!")
    }

    open fun tickOutput(line: String) {}

    open fun tickInput(line: String) {
        if (line.startsWith(config.server.gameCommandPrefix)) {
            serverProcess.outputStream.write((line.removePrefix(config.server.gameCommandPrefix).trim() + "\n").toByteArray(Charset.forName(config.inputCharset)))
            serverProcess.outputStream.flush()
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

    private fun consoleCommand(command: String) {
        val writer = process.outputWriter(Charset.forName(config.inputCharset))
        writer.write(commandHelper.processCommand(command) + "\n")
        writer.flush()
    }

    abstract fun parseConsoleCommandFeedback(line: String): String

    override fun command(command: String, mode: CommandExecutingMode): String =
        when (mode) {
            CommandExecutingMode.RCON -> commandHelper.processCommand(command).let {
                val result = rcon?.command(it) ?: command(it, CommandExecutingMode.CONSOLE)
                logger.debug("[Command] $it -> $result")
                result
            }
            CommandExecutingMode.CONSOLE -> consoleCommand(command).let {
                consoleCommand(command)
                val reader = process.inputStream.bufferedReader()
                return parseConsoleCommandFeedback(reader.readLine().also(::output))
            }
        }

    override fun send(command: String, mode: CommandExecutingMode) {
        when (mode) {
            CommandExecutingMode.RCON -> commandHelper.processCommand(command).let {
                rcon?.command(it) ?: send(it, CommandExecutingMode.CONSOLE)
                logger.debug("[Command] $it")
            }
            CommandExecutingMode.CONSOLE -> consoleCommand(command).let {
                consoleCommand(command)
            }
        }
    }

}
