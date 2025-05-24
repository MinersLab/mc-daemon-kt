package minerslab.mcd.api.command

import minerslab.mcd.api.command.Commands.dispatcher
import starry.adventure.brigadier.dispatcher.DispatcherRegisterContext
import starry.adventure.brigadier.dispatcher.register
import java.util.function.Consumer

object Commands {

    const val PREFIX = "!!"

    private lateinit var dispatcher: ServerCommandDispatcher

    val defaultCommands = mutableSetOf<Consumer<ServerCommandDispatcher>>()

    private fun createDefaultCommands(dispatcher: ServerCommandDispatcher) =
        dispatcher.also {
            defaultCommands.forEach { it.accept(dispatcher) }
        }

    fun reloadDispatcher(newDispatcher: ServerCommandDispatcher) {
        dispatcher = createDefaultCommands(newDispatcher)
    }

    fun register(block: DispatcherRegisterContext<ServerCommandSource>.() -> Unit) = dispatcher.register(block)

    fun remove(vararg names: String) {
        val newDispatcher = ServerCommandDispatcher()
        dispatcher.root.children
            .filterNot { it.name in names }
            .forEach(newDispatcher.root::addChild)
        reloadDispatcher(newDispatcher)
    }

    /**
     * @return 返回 [ServerCommandDispatcher] (注意: 该方法返回的 [dispatcher] 会改变)
     */
    fun getDispatcher() = dispatcher

}
