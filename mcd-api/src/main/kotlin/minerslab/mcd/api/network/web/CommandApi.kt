package minerslab.mcd.api.network.web

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*
import minerslab.mcd.api.command.Commands
import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.mcDaemon

class ApiCommandSource(message: String) : ServerCommandSource(mcDaemon.handler, "Api", message, true)

fun Application.commandAutocomplete() {
    routing {
        get("/mcd/api/command/autocomplete") {
            val command = call.queryParameters["command"] ?: ""
            val cursor = call.queryParameters["cursor"]?.toInt() ?: command.length
            val dispatcher = Commands.getDispatcher()
            val source = ApiCommandSource("${mcDaemon.handler.config.server.daemonCommandPrefix}$command")
            val suggestions = dispatcher.getCompletionSuggestions(
                dispatcher.parse(command, source),
                cursor
            ).get()
            val serialized = buildJsonObject {
                putJsonObject("range") {
                    put("start", suggestions.range.start)
                    put("end", suggestions.range.end)
                }
                putJsonArray("list") {
                    suggestions.list.forEach { suggestion ->
                        addJsonObject {
                            putJsonObject("range") {
                                put("start", suggestions.range.start)
                                put("end", suggestions.range.end)
                            }
                            put("text", suggestion.text)
                            put("tooltip", suggestion.tooltip?.string ?: "")
                        }
                    }
                }
            }
            call.respond(Json.encodeToString(serialized))
        }
    }
}
