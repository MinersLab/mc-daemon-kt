package minerslab.mcd.plugin

import starry.adventure.core.event.EventBus

class PluginLoadingContext(val pluginClassLoader: PluginClassLoader) {

    val eventBus = EventBus("plugin@${pluginClassLoader.meta.id}")

    val pluginListeners = mutableSetOf<PluginListener>()

    companion object {
        internal var current: PluginLoadingContext? = null
        fun get(): PluginLoadingContext = current!!
        fun getOrNull(): PluginLoadingContext? = current
    }

    override fun toString() = "PluginLoadingContext(${pluginClassLoader.meta.id})"

}
