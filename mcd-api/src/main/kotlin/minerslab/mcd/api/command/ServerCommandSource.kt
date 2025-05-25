package minerslab.mcd.api.command

import com.mojang.brigadier.CommandDispatcher
import minerslab.mcd.McDaemon
import minerslab.mcd.api.entity.ServerPlayer
import minerslab.mcd.handler.ServerHandler

class ServerCommandSource(
    val daemon: McDaemon,
    val handler: ServerHandler<*>,
    var sender: String,
    var rawMessage: String,
    var isServer: Boolean = false
) {

    fun sender() = ServerPlayer(handler, sender)

}

typealias ServerCommandDispatcher = CommandDispatcher<ServerCommandSource>
