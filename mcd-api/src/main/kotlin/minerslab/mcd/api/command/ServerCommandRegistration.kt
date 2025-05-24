package minerslab.mcd.api.command

import com.mojang.brigadier.tree.CommandNode
import minerslab.mcd.plugin.PluginListener
import minerslab.mcd.plugin.PluginLoadingContext
import starry.adventure.brigadier.dispatcher.DispatcherRegisterContext

class ServerCommandRegistration(val pluginLoadingContext: PluginLoadingContext) : PluginListener {

    companion object {
        fun getOrNull() = PluginLoadingContext.getOrNull()?.let {
            it.pluginListeners.filterIsInstance<ServerCommandRegistration>().firstOrNull()
                ?: ServerCommandRegistration(it).also(it.pluginListeners::add)
        }
        fun get() = getOrNull()!!
    }

    val commands = mutableSetOf<String>()

    fun register(block: DispatcherRegisterContext<ServerCommandSource>.() -> Unit) = Commands.register {
        val source = dispatcher.root.children.map(CommandNode<*>::getName)
        block()
        val differences = dispatcher.root.children.map(CommandNode<*>::getName)
            .filter { it !in source }
        commands += differences
    }

    override fun dispose() = Commands.remove(*commands.toTypedArray())

}