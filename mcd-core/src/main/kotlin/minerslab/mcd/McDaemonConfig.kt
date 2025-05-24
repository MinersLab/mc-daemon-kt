package minerslab.mcd

import kotlinx.serialization.Serializable

@Serializable
data class McDaemonConfig(
    val handler: String
)
