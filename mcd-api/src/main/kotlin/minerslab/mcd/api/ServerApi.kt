package minerslab.mcd.api

import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.asString
import java.nio.charset.Charset

fun ServerCommandSource.sendCommand(command: String) = handler.run {
    val writer = getProcess().outputWriter(Charset.forName(getConfig().inputCharset))
    writer.write(handler.getCommandHelper().processCommand(command))
    writer.flush()
}

fun ServerCommandSource.sendFeedback(content: List<JsonText>, broadcast: Boolean = false) = handler.run {
    sendCommand(
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

