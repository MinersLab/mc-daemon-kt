package minerslab.mcd.api.command

import com.mojang.brigadier.CommandDispatcher
import minerslab.mcd.api.entity.player.ServerPlayer
import minerslab.mcd.handler.ServerHandler

open class ServerCommandSource(
    val handler: ServerHandler<*>,
    var sender: String,
    var rawMessage: String,
    var isServer: Boolean = false
) {

    open fun sender() = ServerPlayer(handler, sender)

}

typealias ServerCommandDispatcher = CommandDispatcher<ServerCommandSource>
