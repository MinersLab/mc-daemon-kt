package minerslab.mcd.api.command

import com.mojang.brigadier.tree.CommandNode
import minerslab.mcd.api.registry.DeferredRegistry
import minerslab.mcd.plugin.PluginLoadingContext
import starry.adventure.brigadier.dispatcher.DispatcherRegisterContext

/**
 * 插件命令注册入口
 * @see PluginLoadingContext
 */
class ServerCommandRegistration(val pluginLoadingContext: PluginLoadingContext) : DeferredRegistry<Unit, DispatcherRegisterContext<ServerCommandSource>, Unit> {

    companion object : DeferredRegistry.Manager<ServerCommandRegistration>(ServerCommandRegistration::class)

    private val commands = mutableSetOf<String>()

    override fun register(block: DispatcherRegisterContext<ServerCommandSource>.() -> Unit) = Commands.register {
        val source = dispatcher.root.children.map(CommandNode<*>::getName)
        block()
        val differences = dispatcher.root.children.map(CommandNode<*>::getName)
            .filter { it !in source }
        commands += differences
    }

    override fun dispose() = Commands.remove(*commands.toTypedArray())

}
