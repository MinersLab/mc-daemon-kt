package minerslab.mcd.api.command

import starry.adventure.brigadier.dispatcher.DispatcherRegisterContext
import starry.adventure.brigadier.dispatcher.register
import java.util.function.Consumer

/**
 * 命令注册表
 */
object Commands {

    private lateinit var dispatcher: ServerCommandDispatcher

    val defaultCommands = mutableSetOf<Consumer<ServerCommandDispatcher>>()

    private fun createDefaultCommands(dispatcher: ServerCommandDispatcher) =
        dispatcher.also { defaultCommands.forEach { it.accept(dispatcher) } }

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
     * @return 返回 [ServerCommandDispatcher]
     */
    fun getDispatcher() = dispatcher

}
