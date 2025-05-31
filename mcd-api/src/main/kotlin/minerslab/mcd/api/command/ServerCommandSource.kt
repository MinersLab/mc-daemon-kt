package minerslab.mcd.api.command

import com.mojang.brigadier.CommandDispatcher
import minerslab.mcd.api.world.player.ServerPlayer
import minerslab.mcd.handler.ServerHandler

/**
 * 服务器命令会话
 */
open class ServerCommandSource(
    val handler: ServerHandler<*>,
    var sender: String,
    var rawMessage: String,
    var isServer: Boolean = false
) {

    /**
     * 获取触发者
     */
    open fun sender() = ServerPlayer(handler, sender)

}

typealias ServerCommandDispatcher = CommandDispatcher<ServerCommandSource>
