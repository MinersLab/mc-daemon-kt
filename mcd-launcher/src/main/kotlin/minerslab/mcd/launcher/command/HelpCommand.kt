package minerslab.mcd.launcher.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.CommandNode
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.data.feature
import minerslab.mcd.api.sendFeedback
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.text
import minerslab.mcd.launcher.registry.Features
import starry.adventure.brigadier.command.argument
import starry.adventure.brigadier.dispatcher.register
import java.util.function.Consumer

object HelpCommand : Consumer<ServerCommandDispatcher> {

    override fun accept(t: ServerCommandDispatcher) = t.register {
        literal("help") {
            requires(feature(Features.Commands.HELP))
            run {
                for (command in t.root.children) {
                    if (!command.requirement.test(source)) continue
                    val usage = text().green()
                        .a(command.name).a(" ")
                        .gray()
                        .a(t.getSmartUsage(command, source).toList().joinToString(separator = " ") { it.second })
                    val text = JsonText(
                        usage.toString(),
                        clickEvent = JsonText.ClickEvent(
                            JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                            command = "!!help ${command.name}"
                        )
                    )
                    source.sendFeedback(text)
                }
            }
            argument("command", StringArgumentType.greedyString()) {
                suggests {
                    t.root.children
                        .map(CommandNode<*>::getName)
                        .forEach(it::suggest)
                }
                run {
                    val command: String by argument()
                    val node = t.root.getChild(command)
                    if (!node.requirement.test(source)) return@run
                    for (usage in t.getAllUsage(node, source, true)) {
                        val text = JsonText(
                            text().green().a(node.name).a(" ").gray().a(usage).toString(),
                            clickEvent = JsonText.ClickEvent(
                                JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                                command = "!!${node.name} $usage"
                            )
                        )
                        source.sendFeedback(text)
                    }
                }
            }
        }
    }

}