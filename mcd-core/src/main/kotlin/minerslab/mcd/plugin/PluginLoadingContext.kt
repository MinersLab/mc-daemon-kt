package minerslab.mcd.plugin

import starry.adventure.core.event.EventBus

class PluginLoadingContext(val pluginClassLoader: PluginClassLoader) {

    val eventBus = EventBus("plugin@${pluginClassLoader.meta.id}")

    val pluginListeners = mutableSetOf<PluginListener>()

    companion object {
        internal val current = ThreadLocal<PluginLoadingContext>()
        fun get(): PluginLoadingContext = current.get()!!
        fun getOrNull(): PluginLoadingContext? = current.get()
    }

    override fun toString() = "PluginLoadingContext(${pluginClassLoader.meta.id})"

}
