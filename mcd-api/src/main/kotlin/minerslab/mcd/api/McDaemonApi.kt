package minerslab.mcd.api

import minerslab.mcd.api.command.Commands
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.api.config.FeatureConfig
import minerslab.mcd.api.config.useConfig
import minerslab.mcd.api.event.PlayerEvent
import minerslab.mcd.api.event.ServerEvent
import minerslab.mcd.api.network.web.*
import minerslab.mcd.event.EmbeddedServerEvent
import minerslab.mcd.findModule
import minerslab.mcd.handler.ServerHandler
import minerslab.mcd.mcDaemon
import minerslab.mcd.util.Namespaces.MC_DAEMON
import minerslab.mcd.util.addEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starry.adventure.core.registry.Identifiers.div

class McDaemonApi : McDaemonModule {

    companion object

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 管理特性的配置项目
     */
    val features by useConfig<FeatureConfig>(MC_DAEMON / "features.json")

    override fun start() {
        Commands.reloadDispatcher(ServerCommandDispatcher())
        mcDaemon.eventBus.addEventListener(::startEmbeddedServer)
        mcDaemon.handler.eventBus.addEventListener(::onServerMessage)
        mcDaemon.handler.eventBus.addEventListener(::onPlayerMessage)
        logger.info("Initialized")
    }

    private fun startEmbeddedServer(event: EmbeddedServerEvent.StartEvent) = event.application.run {
        commandAutocomplete()
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

    private fun onServerMessage(event: ServerEvent.MessageEvent) {
        handleCommand(event.handler, event.caller, event.message, true)
    }

    private fun onPlayerMessage(event: PlayerEvent.MessageEvent) {
        handleCommand(event.handler, event.caller, event.message, false)
    }

}

val McDaemonApi.Companion.instance
    get() = mcDaemon.findModule<McDaemonApi>()

