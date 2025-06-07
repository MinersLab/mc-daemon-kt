package minerslab.mcd.api.registry

import starry.adventure.registry.IRegistry
import starry.adventure.registry.Identifier
import starry.adventure.registry.ResourceKey

/**
 * 注册信息
 */
class RegistryEntry<T>(
    val registry: ResourceKey<IRegistry<T>>,
    val identifier: Identifier
) {

    @Suppress("UNCHECKED_CAST")
    fun getOrNull() = Registries.REGISTRIES.get(identifier) as? T
    fun get() = getOrNull()!!

    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T { return get() }

}