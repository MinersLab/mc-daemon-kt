package minerslab.mcd.launcher.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.arguments.StringArgumentType.word
import kotlinx.serialization.json.Json
import minerslab.mcd.McDaemonVersion
import minerslab.mcd.api.McDaemonApi
import minerslab.mcd.api.command.IsServer
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.command.or
import minerslab.mcd.api.config.isFutureEnabled
import minerslab.mcd.api.data.Feature
import minerslab.mcd.api.instance
import minerslab.mcd.api.permission.McDaemonPermissionApi
import minerslab.mcd.api.permission.instance
import minerslab.mcd.api.permission.permission
import minerslab.mcd.api.registry.Registries
import minerslab.mcd.api.sendFeedback
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.text
import minerslab.mcd.mcDaemon
import minerslab.mcd.plugin.PluginClassLoader.Companion.DISABLED_PREFIX
import minerslab.mcd.plugin.PluginStatus.*
import minerslab.mcd.util.Namespaces.MC_DAEMON
import minerslab.mcd.util.Namespaces.mcDaemon
import starry.adventure.brigadier.command.argument
import starry.adventure.brigadier.dispatcher.register
import starry.adventure.core.registry.Identifier
import java.util.function.Consumer
import kotlin.io.path.div

object McDaemonCommand : Consumer<ServerCommandDispatcher> {

    override fun accept(t: ServerCommandDispatcher) = t.register {
        literal("mcd") {
            run {
                source.sendFeedback(
                    text().green().a("McDaemon-Kt")
                        .gray()
                        .a(" v${McDaemonVersion.version} ")
                        .darkGray()
                        .a("[${McDaemonVersion.gitBranch}/${McDaemonVersion.gitHash}]")
                )
            }
            literal("stop") {
                requires(permission("command.$MC_DAEMON.mcd.stop") or IsServer)
                run {
                    source.handler.command(source.handler.commandHelper.stop())
                }
            }
            literal("features") {
                requires(permission("command.$MC_DAEMON.mcd.features") or IsServer)
                literal("reload") {
                    run {
                        McDaemonApi.instance.featuresWrapper.reload()
                    }
                }
                argument("name", string()) {
                    suggests {
                        Registries.FEATURES.toList()
                            .map(Pair<Identifier, Feature>::first)
                            .map(Identifier::toString)
                            .map(Json::encodeToString)
                            .forEach(it::suggest)
                    }
                    run {
                        val name: String by argument()
                        val feature = Registries.FEATURES.get(mcDaemon(name))
                        val defaultEnabled = feature?.defaultEnabled ?: false
                        val features = McDaemonApi.instance.features
                        val isEnabled = features.isFutureEnabled(feature.toString(), defaultEnabled)
                        source.sendFeedback(
                            text().run { if (isEnabled) green() else red() }.a(isEnabled)
                        )
                    }
                    literal("enable") {
                        run {
                            val name: String by argument()
                            val feature = Registries.FEATURES.get(mcDaemon(name)) ?: return@run
                            val features = McDaemonApi.instance.features
                            features.enabled.add(feature.toString())
                            features.disabled.remove(feature.toString())
                            McDaemonApi.instance.features = features
                        }
                    }
                    literal("disable") {
                        run {
                            val name: String by argument()
                            val feature = Registries.FEATURES.get(mcDaemon(name)) ?: return@run
                            val features = McDaemonApi.instance.features
                            features.enabled.remove(feature.toString())
                            features.disabled.add(feature.toString())
                            McDaemonApi.instance.features = features
                        }
                    }
                }
            }
            literal("perms") {
                val perms = McDaemonPermissionApi.instance
                requires(permission("command.$MC_DAEMON.mcd.perms") or IsServer)
                literal("reload") {
                    run {
                        perms.configWrapper.reload()
                    }
                }
                literal("group") {
                    run {
                        source.sendFeedback(perms.config.groups.keys.joinToString())
                    }
                    argument("name", word()) {
                        suggests {
                            perms.config.groups.keys.forEach(it::suggest)
                        }
                        literal("list") {
                            run {
                                val name: String by argument()
                                val permissions = perms.getGroupPermissions(name) ?: return@run
                                source.sendFeedback(permissions.keys.joinToString())
                            }
                        }
                        literal("create") {
                            run {
                                val name: String by argument()
                                perms.createGroup(name)
                            }
                        }
                        literal("add-group") {
                            argument("group", word()) {
                                run {
                                    val group: String by argument("name")
                                    val parent: String by argument("group")
                                    perms.addGroupExtends(group, parent)
                                }
                            }
                        }
                        literal("remove-group") {
                            argument("group", word()) {
                                run {
                                    val group: String by argument("name")
                                    val parent: String by argument("group")
                                    perms.removeGroupExtends(group, parent)
                                }
                            }
                        }
                        literal("add") {
                            argument("permission", string()) {
                                argument("value", StringArgumentType.greedyString()) {
                                    run {
                                        val name: String by argument()
                                        val permission: String by argument()
                                        val value: String by argument()
                                        perms.addGroupPermissions(name, permission to Json.parseToJsonElement(value))
                                    }
                                }
                            }
                        }
                        literal("get") {
                            argument("permission", string()) {
                                run {
                                    val name: String by argument()
                                    val permission: String by argument()
                                    source.sendFeedback(
                                        "[$name] $permission = ${
                                            perms.getGroupPermission(
                                                name,
                                                permission
                                            )
                                        }"
                                    )
                                }
                            }
                        }
                        literal("remove") {
                            argument("permission", string()) {
                                run {
                                    val name: String by argument()
                                    val permission: String by argument()
                                    perms.removeGroupPermissions(name, permission)
                                }
                            }
                        }
                    }
                }
                literal("user") {
                    run {
                        source.sendFeedback(perms.config.users.keys.joinToString())
                    }
                    argument("name", word()) {
                        suggests {
                            perms.config.users.keys.forEach(it::suggest)
                        }
                        literal("list") {
                            run {
                                val name: String by argument()
                                val permissions = perms.getUserPermissions(name) ?: return@run
                                source.sendFeedback(permissions.keys.joinToString())
                            }
                        }
                        literal("create") {
                            run {
                                val name: String by argument()
                                perms.createUser(name)
                            }
                        }
                        literal("add-group") {
                            argument("group", word()) {
                                run {
                                    val group: String by argument("name")
                                    val parent: String by argument("group")
                                    perms.addUserGroups(group, parent)
                                }
                            }
                        }
                        literal("remove-group") {
                            argument("group", word()) {
                                run {
                                    val group: String by argument("name")
                                    val parent: String by argument("group")
                                    perms.removeUserGroups(group, parent)
                                }
                            }
                        }
                        literal("add") {
                            argument("permission", string()) {
                                argument("value", StringArgumentType.greedyString()) {
                                    run {
                                        val name: String by argument()
                                        val permission: String by argument()
                                        val value: String by argument()
                                        perms.addUserPermissions(name, permission to Json.parseToJsonElement(value))
                                    }
                                }
                            }
                        }
                        literal("get") {
                            argument("permission", string()) {
                                run {
                                    val name: String by argument()
                                    val permission: String by argument()
                                    source.sendFeedback(
                                        "[$name] $permission = ${
                                            perms.getUserPermission(
                                                name,
                                                permission
                                            )
                                        }"
                                    )
                                }
                            }
                        }
                        literal("remove") {
                            argument("permission", string()) {
                                run {
                                    val name: String by argument()
                                    val permission: String by argument()
                                    perms.removeUserPermissions(name, permission)
                                }
                            }
                        }
                    }
                }
            }
            literal("plugin") {
                requires(permission("command.$MC_DAEMON.mcd.plugin") or IsServer)
                literal("disable") {
                    argument("id", StringArgumentType.greedyString()) {
                        run {
                            val id: String by argument()
                            val file = mcDaemon.pluginManager.plugins[id]!!.file
                            mcDaemon.pluginManager.dispose(id)
                            file.renameTo(file.parentFile.resolve("$DISABLED_PREFIX${file.name}"))
                        }
                    }
                }
                literal("enable") {
                    argument("file", StringArgumentType.greedyString()) {
                        run {
                            val fileName: String by argument("file")
                            val file = (mcDaemon.pluginManager.pluginsPath / fileName).toFile()
                            file.renameTo(file.parentFile.resolve(file.name.removePrefix(DISABLED_PREFIX)))
                            mcDaemon.pluginManager.add(file)
                        }
                    }
                }
                literal("load") {
                    argument("file", StringArgumentType.greedyString()) {
                        run {
                            val file: String by argument()
                            val pluginFile = mcDaemon.pluginManager.pluginsPath.resolve(file).toFile()
                            mcDaemon.pluginManager.add(pluginFile)
                            mcDaemon.pluginManager.reload(pluginFile)
                        }
                    }
                }
                literal("unload") {
                    argument("id", StringArgumentType.greedyString()) {
                        run {
                            val id: String by argument()
                            mcDaemon.pluginManager.dispose(id)
                        }
                    }
                }
                literal("reload") {
                    argument("id", StringArgumentType.greedyString()) {
                        run {
                            val id: String by argument()
                            mcDaemon.pluginManager.reload(id)
                        }
                    }
                }
                literal("list") {
                    literal("unloaded") {
                        run {
                            for ((file, meta) in mcDaemon.pluginManager.scan()) {
                                if (meta.id in mcDaemon.pluginManager.plugins) continue
                                val name = file.relativeTo(mcDaemon.pluginManager.pluginsPath.toFile())
                                val content = "${meta.id} ($name)"
                                val text = JsonText(
                                    text().gray().a(content).toString(),
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
                        for ((pluginName, plugin) in mcDaemon.pluginManager.plugins) {
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
                                    command = "!!mcd plugin load ${plugin.file.relativeTo(mcDaemon.pluginManager.pluginsPath.toFile())}"
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