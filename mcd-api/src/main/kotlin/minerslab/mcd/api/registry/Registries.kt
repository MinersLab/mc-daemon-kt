package minerslab.mcd.api.registry

import minerslab.mcd.util.Namespaces.MC_DAEMON
import starry.adventure.core.registry.IRegistry
import starry.adventure.core.registry.Identifier
import starry.adventure.core.registry.Registry
import starry.adventure.core.registry.ResourceKey
import starry.adventure.core.registry.identifierOf

object Registries {

    object Keys {
        private fun <T> create(name: String) = ResourceKey<IRegistry<T>>(identifierOf("registry", MC_DAEMON), identifierOf(name, MC_DAEMON))

        @JvmStatic
        val REGISTRIES = create<IRegistry<*>>("registry")
    }

    @JvmStatic
    val REGISTRIES = Registry(Keys.REGISTRIES)

    init {
        REGISTRIES.register(identifierOf("registry", MC_DAEMON)) { REGISTRIES }
    }

}

fun <T> Registry<*>.createResourceKey(location: Identifier): ResourceKey<T> =
    ResourceKey(this.getRegistryKey().getLocation(), location)
