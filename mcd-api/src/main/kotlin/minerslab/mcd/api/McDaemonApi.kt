package minerslab.mcd.api

import minerslab.mcd.McDaemon
import minerslab.mcd.api.command.Commands
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.event.ServerEvent
import minerslab.mcd.findModule
import minerslab.mcd.handler.ServerHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import starry.adventure.core.event.WrappedEvent

class McDaemonApi : McDaemonModule {

    init {
        Commands.reloadDispatcher(ServerCommandDispatcher())
    }

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    lateinit var daemon: McDaemon

    override fun start(daemon: McDaemon) {
        this.daemon = daemon
        daemon.handler.eventBus.on(::onServerMessage)
        daemon.handler.eventBus.on(::onPlayerMessage)
        logger.info("Initialized")
    }

    override fun dispose(daemon: McDaemon) {
        this.daemon = daemon
        logger.info("Disposed")
    }

    fun handleCommand(handler: ServerHandler<*>, caller: String, message: String, isServer: Boolean): Int? {
        if (!message.startsWith(Commands.PREFIX)) return null
        try {
            val command = message.removePrefix(Commands.PREFIX)
            val source = ServerCommandSource(daemon, handler, caller, message, isServer)
            return Commands.getDispatcher().execute(command, source).also {
                logger.info("Command Result: $caller [$message] -> $it")
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

val McDaemon.api
    get() = this.findModule<McDaemonApi>()
