package minerslab.mcd.api.registry

import minerslab.mcd.plugin.PluginLoadingContext
import minerslab.mcd.util.Namespaces.MC_DAEMON
import starry.adventure.core.event.EventBus
import starry.adventure.core.registry.Identifier
import starry.adventure.core.registry.Registry
import starry.adventure.core.registry.identifierOf

class DeferredRegister<T>(val registry: Registry<T>) {

    private val caches = mutableMapOf<Identifier, () -> T>()

    fun register(eventBus: EventBus) {
        eventBus.on<RegistryEvent.RegisterEvent<T>> {
            val event = it.unwrap()
            if (event.registry != registry) return@on
            caches.forEach(event.registry::register)
            caches.clear()
        }
    }

    fun register(name: String, callback: () -> T) {
        val identifier = identifierOf(
            name,
            defaultNamespace = PluginLoadingContext.getOrNull()?.pluginClassLoader?.meta?.id ?: MC_DAEMON
        )
        if (identifier in caches) throw IllegalStateException("Duplicate registry '$identifier' of registry '${registry.getRegistryKey()}'")
        caches[identifier] = callback
    }

}
