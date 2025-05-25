package minerslab.mcd.api

import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.asString
import minerslab.mcd.handler.prompt
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import starry.adventure.core.util.Wrapped
import starry.adventure.core.util.wrapped

fun ServerCommandSource.command(command: String) = handler.command(command)
fun ServerCommandSource.prompt(command: String) = handler.prompt(command)

fun ServerCommandSource.sendFeedback(raw: Wrapped<String>, broadcast: Boolean = false) = handler.run {
    if (!isServer || broadcast) command(
        handler.getCommandHelper().tellraw(
            if (broadcast) "@a" else sender,
            raw.unwrap()
        )
    )
    if (isServer || broadcast) {
        val json = JSONComponentSerializer.json().deserialize(raw.unwrap())
        val ansi = ANSIComponentSerializer.ansi().serialize(json)
        val legacy = LegacyComponentSerializer.legacySection().deserialize(ansi)
        val colored = ANSIComponentSerializer.ansi().serialize(legacy)
        println(colored)
    }
}

fun ServerCommandSource.sendFeedback(content: List<JsonText>, broadcast: Boolean = false) =
    sendFeedback(content.asString().wrapped(), broadcast)

fun ServerCommandSource.sendFeedback(content: JsonText, broadcast: Boolean = false) =
    sendFeedback(listOf(content), broadcast)

fun ServerCommandSource.sendFeedback(content: CharSequence, broadcast: Boolean = false) =
    sendFeedback(JsonText(text = String(content.toList().toCharArray())), broadcast)

fun ServerCommandSource.sendFeedback(content: Component, broadcast: Boolean = false) =
    sendFeedback(JSONComponentSerializer.json().serialize(content).wrapped(), broadcast)


