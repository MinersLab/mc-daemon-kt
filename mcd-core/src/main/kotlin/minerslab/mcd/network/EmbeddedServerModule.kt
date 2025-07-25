package minerslab.mcd.network

import io.ktor.server.application.*
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import minerslab.mcd.McDaemon
import minerslab.mcd.McDaemonVersion
import minerslab.mcd.api.McDaemonModule
import minerslab.mcd.event.EmbeddedServerEvent
import minerslab.mcd.mcDaemon
import java.io.File
import kotlin.io.path.div

class EmbeddedServerModule : McDaemonModule {

    val staticFiles: File = (mcDaemon.path / "public").toFile().apply {
        mkdirs()
    }

    override fun start() = mcDaemon.run<McDaemon, Unit> {
        eventBus.on<EmbeddedServerEvent.StartEvent> { it.unwrap().application.initializeEmbeddedServer() }
    }

    fun Application.initializeEmbeddedServer() {
        routing {
            get("/mcd") {
                call.respond(Json.encodeToString(McDaemonVersion.toMap()))
            }
            staticFiles("/public", staticFiles)
        }
    }


}