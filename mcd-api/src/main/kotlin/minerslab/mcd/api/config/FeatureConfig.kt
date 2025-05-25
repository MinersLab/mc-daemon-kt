package minerslab.mcd.api.config

import kotlinx.serialization.Serializable

@Serializable
data class FeatureConfig(
    val enabled: MutableSet<String> = mutableSetOf(),
    val disabled: MutableSet<String> = mutableSetOf()
)
