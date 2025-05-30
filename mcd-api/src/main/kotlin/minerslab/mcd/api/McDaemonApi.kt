package minerslab.mcd.api

import minerslab.mcd.api.command.Commands
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.api.config.FeatureConfig
import minerslab.mcd.api.config.useConfig
import minerslab.mcd.event.ServerEvent
import minerslab.mcd.findModule
import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.mcDaemon
import minerslab.mcd.util.Namespaces.MC_DAEMON
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starry.adventure.core.event.WrappedEvent
import starry.adventure.core.registry.Identifiers.div

class McDaemonApi : McDaemonModule {

    companion object

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    val featureConfig by useConfig<FeatureConfig>(MC_DAEMON / "features.json")

    override fun start() {
        Commands.reloadDispatcher(ServerCommandDispatcher())
        mcDaemon.handler.eventBus.on(::onServerMessage)
        mcDaemon.handler.eventBus.on(::onPlayerMessage)
        logger.info("Initialized")
    }

    override fun dispose() {
        logger.info("Disposed")
    }

    fun handleCommand(handler: ServerHandler<*>, caller: String, message: String, isServer: Boolean): Int? {
        val commandPrefix = handler.config.server.daemonCommandPrefix
        if (!message.startsWith(commandPrefix)) return null
        try {
            val command = message.removePrefix(commandPrefix)
            val source = ServerCommandSource(handler, caller, message, isServer)
            return Commands.getDispatcher().execute(command, source).also {
                logger.info("[$caller] $message -> $it")
            }
        } catch (throwable: Throwable) {
            logger.error("$throwable")
            throwable.printStackTrace()
            return null
        }
    }

    private fun onServerMessage(wrapped: WrappedEvent<ServerEvent.ServerMessageEvent>) {
        val event = wrapped.unwrap()
        handleCommand(event.handler, event.caller, event.message, true)
    }

    private fun onPlayerMessage(wrapped: WrappedEvent<ServerEvent.PlayerMessageEvent>) {
        val event = wrapped.unwrap()
        handleCommand(event.handler, event.caller, event.message, false)
    }

}

val McDaemonApi.Companion.instance
    get() = mcDaemon.findModule<McDaemonApi>()
