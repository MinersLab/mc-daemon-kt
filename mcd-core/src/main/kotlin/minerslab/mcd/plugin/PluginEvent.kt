package minerslab.mcd.plugin

import starry.adventure.core.event.Event

sealed class PluginEvent(val plugin: PluginClassLoader) : Event() {
    class PostConstructEvent(plugin: PluginClassLoader) : PluginEvent(plugin)
    class PostInitializeEvent(plugin: PluginClassLoader) : PluginEvent(plugin)
}
