package minerslab.mcd.plugin

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import minerslab.mcd.McDaemon
import minerslab.mcd.plugin.PluginClassLoader.Companion.DISABLED_PREFIX
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.io.path.walk

class PluginManager(daemon: McDaemon) {

    val pluginsPath: Path = (daemon.path / "plugins").createDirectories()

    val plugins = mutableMapOf<String, PluginClassLoader>()

    fun scan() = iterator {
        for (pluginPath in pluginsPath.walk()) {
            if (!pluginPath.name.endsWith(".jar")) continue
            val pluginFile = pluginPath.toFile()
            yield(pluginFile to (extractMeta(pluginFile) ?: continue))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun extractMeta(file: File): PluginMeta? {
        val zip = ZipFile(file)
        val metaFile = runCatching {
            zip.getInputStream(zip.getEntry("plugin.conf"))
        }.getOrNull() ?: return null
        val meta = Hocon.decodeFromConfig<PluginMeta>(ConfigFactory.parseString(metaFile.readAllBytes().decodeToString()))
        zip.close()
        return meta
    }

    fun reload(name: String) = reload(plugins[name]?.file ?: throw IllegalStateException("Plugin not found: $name"))
    fun reload(file: File) {
        val plugin = getPluginByFile(file) ?: throw IllegalStateException("Plugin not found: $file")
        dispose(plugin.key)
        val meta = extractMeta(file)
        add(file, meta!!)
        val loader = plugins[meta.id] ?: return
        loader.construct()
        loader.initialize()
    }

    fun add(file: File, meta: PluginMeta) {
        if (file.name.startsWith(DISABLED_PREFIX)) return
        val name = meta.id
        if (name in plugins) dispose(name)
        val loader = PluginClassLoader(file, meta)
        plugins[name] = loader
    }

    fun add(file: File) = extractMeta(file)?.let { add(file, it) }

    fun dispose(name: String) {
        plugins[name]?.close()
        plugins.remove(name)
    }

    fun construct() {
        plugins.values.forEach(::construct)
    }

    fun load(plugin: PluginClassLoader) {
        if (plugin.status == PluginStatus.LOADED) return
        for (dependency in plugin.meta.dependencies) {
            val plugin = getPluginByName(dependency.name) ?: throw IllegalStateException("Expected dependency '${dependency}', but found NOTHING")
            require(dependency.versionRange?.test(plugin.value.meta.version) != false) {
                "Expected dependency '$dependency', but found ${plugin.value.meta.version}"
            }
            load(plugin.value)
        }
        PluginLoadingContext.current.set(plugin.pluginLoadingContext)
        plugin.initialize()
        PluginLoadingContext.current.set(null)
    }

    fun construct(plugin: PluginClassLoader) {
        if (plugin.status != PluginStatus.IDLE) return
        for (dependency in plugin.meta.dependencies) {
            val plugin = getPluginByName(dependency.name) ?: throw IllegalStateException("Expected dependency '${dependency}', but found NOTHING")
            require(dependency.versionRange?.test(plugin.value.meta.version) != false) {
                "Expected dependency '$dependency', but found ${plugin.value.meta.version}"
            }
            construct(plugin.value)
        }
        PluginLoadingContext.current.set(plugin.pluginLoadingContext)
        plugin.construct()
        PluginLoadingContext.current.set(null)
    }

    fun load() {
        plugins.values.forEach(::load)
    }

    fun dispose() {
        plugins.keys.forEach(::dispose)
    }

    fun getPluginByFile(file: File) = plugins.entries.firstOrNull { it.value.file == file }
    fun getPluginByName(name: String) = plugins[name]?.file?.let { getPluginByFile(it) }

}