package org.example

import com.mojang.brigadier.arguments.StringArgumentType
import kotlinx.serialization.json.Json
import minerslab.mcd.api.command.ServerCommandRegistration
import minerslab.mcd.api.sendCommand
import minerslab.mcd.plugin.Plugin
import minerslab.mcd.plugin.PluginEvent
import minerslab.mcd.plugin.PluginLoadingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starry.adventure.brigadier.command.argument

object ExamplePlugin : Plugin {

    val context = PluginLoadingContext.get()

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    init {
        context.eventBus.on<PluginEvent.PostConstructEvent> {
            logger.info("${it.unwrap()}")
        }
    }

    override fun initialize() {
        ServerCommandRegistration.get().register {
            literal("echo") {
                argument("text", StringArgumentType.greedyString()) {
                    run {
                        val text: String by argument()
                        source.sendCommand("tellraw @a ${Json.encodeToString(text)}")
                    }
                }
            }
        }
        logger.info("$context")
        logger.info("Initialized")
        context.eventBus.on<PluginEvent.PostInitializeEvent> {
            logger.info("${it.unwrap()}")
        }
    }

    override fun dispose() {
        logger.info("Disposed")
    }

}
