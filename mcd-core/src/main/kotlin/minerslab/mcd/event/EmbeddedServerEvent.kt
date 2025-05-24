package minerslab.mcd.event

import io.ktor.server.application.Application
import minerslab.mcd.EmbeddedServerType
import starry.adventure.core.event.Event

sealed class EmbeddedServerEvent(val embeddedServer: EmbeddedServerType) : Event() {

    class StartEvent(embeddedServer: EmbeddedServerType, val application: Application) : EmbeddedServerEvent(embeddedServer)

}
