package minerslab.mcd.plugin

import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

class PluginClassLoader(val file: File, val meta: PluginMeta) : URLClassLoader(arrayOf(file.toURI().toURL())) {

    companion object {
        const val DISABLED_PREFIX = "[DISABLED] "
    }

    val pluginLoadingContext = PluginLoadingContext(this)
    var instance: Plugin? = null
    var status: PluginStatus = PluginStatus.IDLE

    fun isEnabled() = !file.name.startsWith(DISABLED_PREFIX)

    fun construct() {
        if (!isEnabled()) return
        try {
            status = PluginStatus.LOADING
            val entrypoint = loadClass(meta.entrypoint)?.kotlin
                ?: throw IllegalStateException("Unable to find entrypoint '${meta.entrypoint}' of plugin '${meta.id}'")
            if (!entrypoint.isSubclassOf(Plugin::class)) throw IllegalStateException("Not a plugin class: ${meta.entrypoint}")
            val newInstance = if (entrypoint.objectInstance != null) entrypoint.objectInstance
            else entrypoint.primaryConstructor?.call()
                ?: throw UnsupportedOperationException("Unable to construct plugin class '${meta.entrypoint}'")
            instance = newInstance as Plugin
            pluginLoadingContext.eventBus.emit(PluginEvent.PostConstructEvent(this))
        } catch (error: Throwable) {
            status = PluginStatus.ERROR
            throw error
        }
    }

    fun initialize() {
        if (!isEnabled()) return
        instance?.initialize()
        status = PluginStatus.LOADED
        pluginLoadingContext.eventBus.emit(PluginEvent.PostInitializeEvent(this))
    }

    override fun close() {
        pluginLoadingContext.pluginListeners.forEach(PluginListener::dispose)
        instance?.dispose()
        status = PluginStatus.CLOSED
        super.close()
    }

}