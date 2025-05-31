package minerslab.mcd.plugin

import starry.adventure.core.event.EventBus

class PluginLoadingContext(val pluginClassLoader: PluginClassLoader) {

    /**
     * 插件事件总线
     */
    val eventBus = EventBus("plugin@${pluginClassLoader.meta.id}")

    /**
     * 插件监听器
     */
    val pluginListeners = mutableSetOf<PluginListener>()

    companion object {
        internal var current: PluginLoadingContext? = null

        /**
         * 在插件中获取插件上下文
         */
        fun get(): PluginLoadingContext = current!!

        /**
         * 在插件中获取插件上下文
         */
        fun getOrNull(): PluginLoadingContext? = current
    }

    override fun toString() = "PluginLoadingContext(${pluginClassLoader.meta.id})"

}
