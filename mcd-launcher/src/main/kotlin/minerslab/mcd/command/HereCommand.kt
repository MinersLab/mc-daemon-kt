package minerslab.mcd.command

import minerslab.mcd.api.command.IsPlayer
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.command.and
import minerslab.mcd.api.command.feature
import minerslab.mcd.api.sendFeedback
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.text
import minerslab.mcd.util.Namespaces.MC_DAEMON
import minerslab.mcd.util.Namespaces.MINECRAFT
import starry.adventure.brigadier.dispatcher.register
import starry.adventure.core.registry.Identifier
import java.text.DecimalFormat
import java.util.function.Consumer

object HereCommand : Consumer<ServerCommandDispatcher> {

    val maps = mutableListOf<(name: String, pos: Triple<Double, Double, Double>, dimension: Identifier) -> JsonText>(
        ::createXaeroWaypoint, ::createVoxelWaypoint
    )

    fun createXaeroWaypoint(name: String, pos: Triple<Double, Double, Double>, dimension: Identifier): JsonText {
        val command =
            "xaero-waypoint:$name's Location:S:${pos.first.toInt()}:${pos.second.toInt()}:${pos.third.toInt()}:6:false:0:Internal-${if (dimension.getNamespace() == MINECRAFT) dimension.getPath() else "dim-%${dimension.getNamespace()}$${dimension.getPath()}"}-waypoints"
        return JsonText(
            text().red().a("[+X]").toString(),
            clickEvent = JsonText.ClickEvent(
                JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                command = command
            ),
            hoverEvent = JsonText.HoverEvent(
                JsonText.HoverEvent.Action.SHOW_TEXT,
                value = listOf(JsonText("Xaero's Map"))
            )
        )
    }

    fun createVoxelWaypoint(name: String, pos: Triple<Double, Double, Double>, dimension: Identifier): JsonText {
        val command =
            "[name:$name's Location, x:${pos.first.toInt()}, y:${pos.second.toInt()}, z:${pos.third.toInt()}, dim:$dimension]"
        return JsonText(
            text().blue().a("[+V]").toString(),
            clickEvent = JsonText.ClickEvent(
                JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                command = command
            ),
            hoverEvent = JsonText.HoverEvent(
                JsonText.HoverEvent.Action.SHOW_TEXT,
                value = listOf(JsonText("Voxel Map"))
            )
        )
    }

    override fun accept(t: ServerCommandDispatcher) = t.register {
        literal("here") {
            requires(feature("command.$MC_DAEMON.here") and IsPlayer)
            run {
                val player = source.sender()
                val dimension = player.getDimension()
                val pos = player.getPosition()
                val decimalFormat = DecimalFormat("0.00")
                val text = JsonText(
                    text().yellow().a(player.name)
                        .green().a(" @ ")
                        .gray().a(dimension.toString())
                        .aqua().a(
                            " [${decimalFormat.format(pos.first)} ${decimalFormat.format(pos.second)} ${
                                decimalFormat.format(pos.third)
                            }]"
                        )
                        .toString(),
                    clickEvent = JsonText.ClickEvent(
                        JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                        command = "/execute in $dimension run tp ${pos.third} ${pos.second} ${pos.third}"
                    )
                )
                val components = mutableListOf(text)
                for (map in maps) {
                    components.add(JsonText(" "))
                    components.add(map(player.name, pos, dimension))
                }
                source.sendFeedback(components, true)
            }
        }
    }

}