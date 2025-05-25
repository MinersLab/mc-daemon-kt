package minerslab.mcd.handler

import minerslab.mcd.event.ServerEvent
import minerslab.mcd.handler.helper.CommandHelper

@ServerHandler.Config(AbstractServerHandler.AbstractServerConfig::class)
open class VanillaServerHandler : AbstractServerHandler<AbstractServerHandler.AbstractServerConfig>() {

    companion object {
        val playerMessageRegex =
            """^\[.*?] \[.*?]:\s+(?:\[Not Secure]\s+)?<([^>]+)>(.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val playerSayMessageRegex =
            """^\[.*?] \[.*?]:\s+(?:\[Not Secure]\s+)?\[([^>]+)](.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val rconStartedMessageRegex =
            """^\[[^]]+] \[[^]]+]: Thread RCON Listener started$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val consoleCommandResultRegex =
            """^\[[^]]+] \[[^]]+]: (.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)

        val serverNames = mutableListOf("Server", "Rcon")
    }

    override fun isPlayerMessage(line: String) = line.matches(playerMessageRegex) || line.matches(playerSayMessageRegex)
    override fun isServerMessage(line: String): Boolean = playerSayMessageRegex.matchEntire(line)?.groupValues?.getOrNull(1)?.trim() in serverNames
    override fun isRconStarted(line: String) = line.matches(rconStartedMessageRegex)

    override fun parseConsoleCommandFeedback(line: String): String {
        val result = playerMessageRegex.matchEntire(line) ?: return ""
        return result.groupValues[1]
    }

    override fun handlePlayerMessage(line: String) {
        val result = playerMessageRegex.matchEntire(line) ?: playerSayMessageRegex.matchEntire(line) ?: return
        val caller = result.groupValues[1]
        val message = result.groupValues[2].trimStart() // 去掉开头的一个空格
        eventBus.emit(ServerEvent.PlayerMessageEvent(this, caller, message))
    }

    override fun handleServerMessage(line: String) {
        val result = playerSayMessageRegex.matchEntire(line) ?: return
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

}