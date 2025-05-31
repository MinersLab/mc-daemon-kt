package minerslab.mcd.api.event

import minerslab.mcd.handler.ServerHandler
import starry.adventure.core.event.Event

sealed class ServerEvent(val handler: ServerHandler<*>) : Event() {

    class MessageEvent(handler: ServerHandler<*>, val caller: String, val message: String) : ServerEvent(handler)

}