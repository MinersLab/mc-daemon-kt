package minerslab.mcd.event

import minerslab.mcd.handler.ServerHandler
import starry.adventure.core.event.Event

sealed class HandlerEvent(val handler: ServerHandler<*>) : Event() {

    class OutputEvent(handler: ServerHandler<*>, val line: String) : HandlerEvent(handler)

}
