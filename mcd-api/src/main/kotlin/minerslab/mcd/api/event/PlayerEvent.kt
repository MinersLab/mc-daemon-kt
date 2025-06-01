package minerslab.mcd.api.event

import minerslab.mcd.api.world.player.Player
import minerslab.mcd.handler.ServerHandler
import starry.adventure.core.event.Event

sealed class PlayerEvent(val handler: ServerHandler<*>, val playerName: String) : Event() {

    val player: Player
        get() = Player(handler, playerName)

    class MessageEvent(handler: ServerHandler<*>, val caller: String, val message: String) : PlayerEvent(handler, caller)
    class JoinEvent(handler: ServerHandler<*>, playerName: String) : PlayerEvent(handler, playerName)

}