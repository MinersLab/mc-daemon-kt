package minerslab.mcd.handler

import minerslab.mcd.event.ServerEvent
import minerslab.mcd.handler.helper.CommandHelper

@ServerHandler.Config(AbstractServerHandler.AbstractServerConfig::class)
open class VanillaServerHandler : AbstractServerHandler<AbstractServerHandler.AbstractServerConfig>() {

    val playerMessageRegex = """^\[.*?] \[.*?]:\s+(?:\[Not Secure]\s+)?<([^>]+)>(.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    val serverMessageRegex = """^\[.*?] \[.*?]:\s+(?:\[Not Secure]\s+)?\[([^>]+)](.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)

    override fun isPlayerMessage(line: String) = line.matches(playerMessageRegex)
    override fun isServerMessage(line: String) = line.matches(serverMessageRegex)

    override fun handlePlayerMessage(line: String) {
        val result = playerMessageRegex.matchEntire(line) ?: return
        val caller = result.groupValues[1]
        val message = result.groupValues[2].trimStart() // 去掉开头的一个空格
        eventBus.emit(ServerEvent.PlayerMessageEvent(this, caller, message))
    }

    override fun handleServerMessage(line: String) {
        val result = serverMessageRegex.matchEntire(line) ?: return
        val caller = result.groupValues[1]
        val message = result.groupValues[2].trimStart() // 去掉开头的一个空格
        eventBus.emit(ServerEvent.ServerMessageEvent(this, caller, message))
    }

    override fun getCommandHelper() = object : CommandHelper {}

}