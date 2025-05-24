package minerslab.mcd.event

import minerslab.mcd.handler.ServerHandler
import starry.adventure.core.event.Event

sealed class ServerEvent(val handler: ServerHandler<*>) : Event() {

    class PlayerMessageEvent(handler: ServerHandler<*>, val caller: String, val message: String) : ServerEvent(handler)
    class ServerMessageEvent(handler: ServerHandler<*>, val caller: String, val message: String) : ServerEvent(handler)

}