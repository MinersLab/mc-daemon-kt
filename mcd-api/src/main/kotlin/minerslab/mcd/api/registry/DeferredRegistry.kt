package minerslab.mcd.api.registry

import minerslab.mcd.plugin.PluginListener
import minerslab.mcd.plugin.PluginLoadingContext
import kotlin.reflect.KClass

/**
 * 插件注册入口
 */
interface DeferredRegistry<T, U, R> : PluginListener {

    open class Manager<T : DeferredRegistry<*, *, *>>(private val clazz: KClass<T>) {
        open fun getOrNull() = PluginLoadingContext.getOrNull()?.let {
            it.pluginListeners.filterIsInstance(clazz.java).firstOrNull()
                ?: (clazz.constructors.first().call(it)).also(it.pluginListeners::add)
        }

        fun get() = getOrNull()!!
    }

    fun register(block: U.() -> T): R

}