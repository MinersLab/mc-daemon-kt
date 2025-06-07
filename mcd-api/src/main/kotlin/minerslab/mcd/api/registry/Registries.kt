package minerslab.mcd.api.registry

import minerslab.mcd.api.data.Feature
import minerslab.mcd.util.Namespaces.MC_DAEMON
import starry.adventure.registry.*

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

        /**
         * 特性注册键
         * @see ResourceKey
         * @see Registry
         */
        @JvmStatic
        val FEATURE = create<Feature>("feature")
    }

    /**
     * 注册表注册
     * @see ResourceKey
     * @see Registry
     */
    @JvmStatic
    val REGISTRIES = Registry(Keys.REGISTRY)

    /**
     * 特性注册
     * @see ResourceKey
     * @see Registry
     */
    @JvmStatic
    val FEATURES = Registry(Keys.FEATURE)

    init {
        REGISTRIES.register(Keys.REGISTRY.getLocation()) { REGISTRIES }
        REGISTRIES.register(Keys.FEATURE.getLocation()) { FEATURES }
    }

}

fun <T> Registry<*>.createResourceKey(location: Identifier): ResourceKey<T> =
    ResourceKey(this.getRegistryKey().getLocation(), location)
