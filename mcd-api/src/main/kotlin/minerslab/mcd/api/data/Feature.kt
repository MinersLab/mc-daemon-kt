package minerslab.mcd.api.data

import minerslab.mcd.api.McDaemonApi
import minerslab.mcd.api.command.requirement
import minerslab.mcd.api.config.FeatureConfig
import minerslab.mcd.api.config.isFutureEnabled
import minerslab.mcd.api.instance
import minerslab.mcd.api.registry.Registries
import starry.adventure.registry.Identifier

/**
 * 特性数据类
 * @param defaultEnabled 当传入值为 `false`，根据 [FeatureConfig.enabled] 判断；当传入值为 `true`，根据 [FeatureConfig.disabled] 判断
 */
class Feature(val defaultEnabled: Boolean = true) {

    override fun toString(): String {
        val key = Registries.FEATURES.get(this) ?: return ""
        val pathList = key.toPathList()
        val type = pathList.first()
        val path = pathList.drop(1).joinToString(".")
        return key.format { namespace, _ -> "$type.$namespace.$path" }
    }

}


/**
 * 要求特性必须被启用
 * @param name 特性名称
 */
fun feature(name: Identifier) = requirement {
    val feature = Registries.FEATURES.get(name) ?: return@requirement false
    feature(feature)()
}


/**
 * 要求特性必须被启用
 * @param feature 特性
 */
fun feature(feature: Feature) = requirement {
    val name = feature.toString()
        .takeUnless(String::isEmpty)
        ?: return@requirement feature.defaultEnabled
    McDaemonApi.instance.features.isFutureEnabled(name, feature.defaultEnabled)
}
