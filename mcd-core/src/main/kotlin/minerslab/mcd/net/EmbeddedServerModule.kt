package minerslab.mcd.net

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import minerslab.mcd.McDaemon
import minerslab.mcd.api.McDaemonModule
import minerslab.mcd.event.EmbeddedServerEvent

class EmbeddedServerModule : McDaemonModule {

    override fun start(daemon: McDaemon) = daemon.run {
        eventBus.on<EmbeddedServerEvent.StartEvent> { it.unwrap().application.initializeEmbeddedServer() }
        Unit
    }

    fun Application.initializeEmbeddedServer() {
        routing {
            get("/mcd/api/v1/ping") {
                call.respond("Pong")
            }
        }
    }


}