package minerslab.mcd.api.registry

import minerslab.mcd.util.Namespaces.MC_DAEMON
import starry.adventure.core.registry.IRegistry
import starry.adventure.core.registry.Identifier
import starry.adventure.core.registry.Registry
import starry.adventure.core.registry.ResourceKey
import starry.adventure.core.registry.identifierOf

/**
 * 注册表
 */
object Registries {

    /**
     * 注册键
     */
    object Keys {
        private fun <T> create(name: String) = ResourceKey<IRegistry<T>>(identifierOf("registry", MC_DAEMON), identifierOf(name, MC_DAEMON))

        /**
         * 注册表注册键
         * @see ResourceKey
         * @see Registry
         */
        @JvmStatic
        val REGISTRY = create<IRegistry<*>>("registry")
    }

    /**
     * 注册表注册
     * @see ResourceKey
     * @see Registry
     */
    @JvmStatic
    val REGISTRIES = Registry(Keys.REGISTRY)

    init {
        REGISTRIES.register(identifierOf("registry", MC_DAEMON)) { REGISTRIES }
    }

}

fun <T> Registry<*>.createResourceKey(location: Identifier): ResourceKey<T> =
    ResourceKey(this.getRegistryKey().getLocation(), location)
