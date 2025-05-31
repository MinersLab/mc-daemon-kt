package minerslab.mcd.api.registry

import minerslab.mcd.plugin.PluginLoadingContext
import starry.adventure.core.registry.IRegistry
import starry.adventure.core.registry.Identifier
import starry.adventure.core.registry.ResourceKey

class DeferredRegister<T>(private val context: PluginLoadingContext, val registryKey: ResourceKey<IRegistry<T>>) : DeferredRegistry<Pair<Identifier, T>, Unit, RegistryEntry<*>> {

    private val entries = mutableMapOf<Identifier, T>()

    @Suppress("UNCHECKED_CAST")
    override fun register(block: Unit.() -> Pair<Identifier, T>): RegistryEntry<T> {
        val registry = Registries.REGISTRIES.get(registryKey.getLocation())!! as IRegistry<T>
        val entry = block(Unit)
        if (entry.first in entries) {
            throw IllegalArgumentException("Identifier ${entry.first} is already registered in the registry ${registry.getRegistryKey()}")
        } else {
            entries[entry.first] = entry.second
            registry.register(entry.first) { entry.second }
            return RegistryEntry(registry.getRegistryKey(), entry.first)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <I : T> register(name: String, block: () -> I): RegistryEntry<I> = register {
        Identifier(context.pluginClassLoader.meta.id, name) to block()
    } as RegistryEntry<I>

    @Suppress("UNCHECKED_CAST")
    override fun dispose() {
        val registry = (Registries.REGISTRIES.get(registryKey.getLocation()) ?: return) as IRegistry<T>
        entries
            .keys
            .forEach(registry::unregister)
    }
    
}

fun <T> IRegistry<T>.deferred() = getRegistryKey().deferred()!!

@Suppress("UNCHECKED_CAST")
fun <T> ResourceKey<IRegistry<T>>.deferred(): DeferredRegister<T>? = PluginLoadingContext.getOrNull()?.let {
    (it.pluginListeners.filterIsInstance(DeferredRegister::class.java)
        .firstOrNull { register -> register.registryKey == this }
        ?: DeferredRegister(it, this)
            .also(it.pluginListeners::add)
    ) as DeferredRegister<T>
}
