package minerslab.mcd.api

import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.asString
import minerslab.mcd.handler.prompt

fun ServerCommandSource.command(command: String) = handler.command(command)
fun ServerCommandSource.prompt(command: String) = handler.prompt(command)

fun ServerCommandSource.sendFeedback(content: List<JsonText>, broadcast: Boolean = false) = handler.run {
    command(
        handler.getCommandHelper().tellraw(
            if (broadcast) "@a" else sender,
            content.asString()
        )
    )
}

fun ServerCommandSource.sendFeedback(content: JsonText, broadcast: Boolean = false) =
    sendFeedback(listOf(content), broadcast)

fun ServerCommandSource.sendFeedback(content: CharSequence, broadcast: Boolean = false) =
    sendFeedback(JsonText(text = String(content.toList().toCharArray())), broadcast)

