package minerslab.mcd.command

import com.mojang.brigadier.arguments.StringArgumentType
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.permission.permission
import minerslab.mcd.api.sendFeedback
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.text
import minerslab.mcd.plugin.PluginClassLoader.Companion.DISABLED_PREFIX
import minerslab.mcd.plugin.PluginStatus.*
import starry.adventure.brigadier.command.argument
import starry.adventure.brigadier.dispatcher.register
import java.util.function.Consumer
import kotlin.collections.iterator
import kotlin.io.path.div

object McDaemonCommand : Consumer<ServerCommandDispatcher> {

    override fun accept(t: ServerCommandDispatcher) = t.register {
        literal("mcd") {
            requires(permission("command.mcd"))
            literal("plugin") {
                literal("disable") {
                    argument("id", StringArgumentType.greedyString()) {
                        run {
                            val id: String by argument()
                            val file = source.daemon.pluginManager.plugins[id]!!.file
                            source.daemon.pluginManager.dispose(id)
                            file.renameTo(file.parentFile.resolve("$DISABLED_PREFIX${file.name}"))
                        }
                    }
                }
                literal("enable") {
                    argument("file", StringArgumentType.greedyString()) {
                        run {
                            val fileName: String by argument("file")
                            val file = (source.daemon.pluginManager.pluginsPath / fileName).toFile()
                            file.renameTo(file.parentFile.resolve(file.name.removePrefix(DISABLED_PREFIX)))
                            source.daemon.pluginManager.add(file)
                        }
                    }
                }
                literal("load") {
                    argument("file", StringArgumentType.greedyString()) {
                        run {
                            val file: String by argument()
                            val pluginFile = source.daemon.pluginManager.pluginsPath.resolve(file).toFile()
                            source.daemon.pluginManager.add(pluginFile)
                            source.daemon.pluginManager.reload(pluginFile)
                        }
                    }
                }
                literal("unload") {
                    argument("id", StringArgumentType.greedyString()) {
                        run {
                            val id: String by argument()
                            source.daemon.pluginManager.dispose(id)
                        }
                    }
                }
                literal("list") {
                    literal("unloaded") {
                        run {
                            for ((file, meta) in source.daemon.pluginManager.scan()) {
                                if (meta.id in source.daemon.pluginManager.plugins) continue
                                val name = file.relativeTo(source.daemon.pluginManager.pluginsPath.toFile())
                                val text = JsonText(
                                    text().gray().a("${meta.id} ($name)").toString(),
                                    clickEvent = JsonText.ClickEvent(
                                        JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                                        command = "!!mcd plugin ${if (file.name.startsWith(DISABLED_PREFIX)) "enable" else "load"} $name"
                                    )
                                )
                                source.sendFeedback(text)
                            }
                        }
                    }
                    run {
                        for ((pluginName, plugin) in source.daemon.pluginManager.plugins) {
                            val text = text()
                            when (plugin.status) {
                                IDLE -> text.darkGray()
                                ERROR -> text.red()
                                LOADING -> text.italic()
                                LOADED -> text.green()
                                CLOSED -> text.gray()
                            }
                            var jsonText = JsonText(
                                text.copy().a(pluginName).toString(),
                                hoverEvent = JsonText.HoverEvent(
                                    JsonText.HoverEvent.Action.SHOW_TEXT,
                                    value = listOf(JsonText(text.copy().a(plugin.status.name).toString()))
                                )
                            )
                            if (plugin.status == IDLE) jsonText = jsonText.copy(
                                clickEvent = JsonText.ClickEvent(
                                    JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                                    command = "!!mcd plugin load ${plugin.file.relativeTo(source.daemon.pluginManager.pluginsPath.toFile())}"
                                )
                            )
                            if (plugin.status == LOADED) jsonText = jsonText.copy(
                                clickEvent = JsonText.ClickEvent(
                                    JsonText.ClickEvent.Action.SUGGEST_COMMAND,
                                    command = "!!mcd plugin disable ${plugin.meta.id}"
                                )
                            )
                            source.sendFeedback(jsonText)
                        }
                    }
                }
            }
        }
    }

}