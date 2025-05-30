package minerslab.mcd.common.handler

import kotlinx.io.IOException
import minerslab.mcd.common.util.createConsoleReader
import minerslab.mcd.event.ServerEvent
import minerslab.mcd.handler.AbstractServerConfig
import minerslab.mcd.handler.AbstractServerHandler
import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.handler.helper.CommandHelper
import org.jline.reader.UserInterruptException
import java.nio.charset.Charset

@ServerHandler.Config(AbstractServerConfig::class)
open class VanillaServerHandler : AbstractServerHandler<AbstractServerConfig>() {

    companion object {
        val PLAYER_MESSAGE_REGEX =
            """^\[.*?] \[.*?]:\s+(?:\[Not Secure]\s+)?<([^>]+)>(.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val PLAYER_SAY_MESSAGE_REGEX =
            """^\[.*?] \[.*?]:\s+(?:\[Not Secure]\s+)?\[([^>]+)](.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val RCON_STARTED_MESSAGE_REGEX =
            """^\[[^]]+] \[[^]]+]: Thread RCON Listener started$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val CONSOLE_COMMAND_RESULT_REGEX =
            """^\[[^]]+] \[[^]]+]: (.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)

        val SERVER_NAMES = mutableListOf("Server", "Rcon")
    }


    override fun parseConsoleCommandFeedback(line: String): String {
        val result = CONSOLE_COMMAND_RESULT_REGEX.matchEntire(line) ?: return ""
        return result.groupValues[1]
    }

    protected fun isPlayerMessage(line: String) = line.matches(PLAYER_MESSAGE_REGEX) || line.matches(PLAYER_SAY_MESSAGE_REGEX)
    protected fun isServerMessage(line: String): Boolean =
        PLAYER_SAY_MESSAGE_REGEX.matchEntire(line)?.groupValues?.getOrNull(1)?.trim() in SERVER_NAMES

    protected fun isRconStarted(line: String) = line.matches(RCON_STARTED_MESSAGE_REGEX)

    protected fun handlePlayerMessage(line: String) {
        val result = PLAYER_MESSAGE_REGEX.matchEntire(line) ?: PLAYER_SAY_MESSAGE_REGEX.matchEntire(line) ?: return
        val caller = result.groupValues[1]
        val message = result.groupValues[2].trimStart() // 去掉开头的一个空格
        eventBus.emit(ServerEvent.PlayerMessageEvent(this, caller, message))
    }

    protected fun handleServerMessage(line: String) {
        val result = PLAYER_SAY_MESSAGE_REGEX.matchEntire(line) ?: return
        val caller = result.groupValues[1]
        val message = result.groupValues[2].trimStart() // 去掉开头的一个空格
        eventBus.emit(ServerEvent.ServerMessageEvent(this, caller, message))
    }

    override fun getCommandHelper() = VanillaCommandHelper

    object VanillaCommandHelper : CommandHelper {

        override fun processDataGet(raw: String) = raw.split(":")
            .toMutableList()
            .drop(1)
            .joinToString(separator = ":")
            .trim()

    }

    override fun tickInput(line: String) {
        if (line.startsWith(config.server.gameCommandPrefix)) {
            super.tickInput(line)
        } else {
            eventBus.emit(ServerEvent.ServerMessageEvent(this, "Server", "${config.server.daemonCommandPrefix}$line"))
        }
    }

    override fun tickOutput(line: String) {
        if (isServerMessage(line)) handleServerMessage(line)
        else if (isPlayerMessage(line)) handlePlayerMessage(line)
        else if (config.rcon.enabled && rcon == null && isRconStarted(line)) startRcon()
    }

    override fun createInputThread() = Thread {
        val reader = createConsoleReader { it.encoding(Charset.forName(config.inputCharset)) }
            .build()
        while (true) {
            try {
                val line = reader.readLine().trimEnd()
                tickInput(line)
            } catch (e: UserInterruptException) {
                break
            } catch (e: IOException) {
                logger.error("Failed to write to server process", e)
                break
            }
        }
    }

}