package minerslab.mcd.api.config

import kotlinx.serialization.Serializable

@Serializable
data class FeatureConfig(
    val enabled: MutableSet<String> = mutableSetOf(),
    val disabled: MutableSet<String> = mutableSetOf()
)

/**
 * 判断特性必须被启用
 * @param name 特性名称
 * @param defaultEnabled 当传入值为 `false`，根据 [FeatureConfig.enabled] 判断；当传入值为 `true`，根据 [FeatureConfig.disabled] 判断
 */
fun FeatureConfig.isFutureEnabled(name: String, defaultEnabled: Boolean = true) =
    if (defaultEnabled) name !in disabled
    else name in enabled
