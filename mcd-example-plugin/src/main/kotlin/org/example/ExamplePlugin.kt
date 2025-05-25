package org.example

import com.mojang.brigadier.arguments.StringArgumentType
import minerslab.mcd.api.command.ServerCommandRegistration
import minerslab.mcd.api.config.usePluginConfig
import minerslab.mcd.api.sendFeedback
import minerslab.mcd.plugin.Plugin
import minerslab.mcd.plugin.PluginEvent
import minerslab.mcd.plugin.PluginLoadingContext
import minerslab.mcd.util.addEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starry.adventure.brigadier.command.argument

object ExamplePlugin : Plugin {

    val context = PluginLoadingContext.get()
    val eventBus = context.eventBus

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    init {
        eventBus.addEventListener(::postConstruct)
        eventBus.addEventListener(::postInitialize)
    }

    private fun postConstruct(event: PluginEvent.PostConstructEvent) {
        logger.info("[PostConstruct] $event")
    }

    private fun postInitialize(event: PluginEvent.PostInitializeEvent) {
        logger.info("[PostInitialize] $event")
    }

    private fun registerCommands() = ServerCommandRegistration.get().register {
        literal("echo") {
            argument("text", StringArgumentType.greedyString()) {
                run {
                    val text: String by argument()
                    source.sendFeedback(text)
                }
            }
        }
    }

    override fun initialize() {
        registerCommands()
        var config: ExampleConfig by context.usePluginConfig("example.json")
        logger.info("[Context] $context")
        logger.info("Initialized")
        logger.info(config.message)
    }

    override fun dispose() {
        logger.info("Disposed")
    }

}
