package minerslab.mcd.plugin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import starry.adventure.parser.impl.VersionRange
import starry.adventure.parser.impl.parseVersionRange

@Serializable
data class PluginMeta(
    val id: String,
    val version: String,
    val entrypoint: String,
    val dependencies: List<Dependency> = listOf()
) {

    @Serializable(with = Dependency.DependencySerializer::class)
    data class Dependency(val name: String, val versionRange: VersionRange? = null) {

        object DependencySerializer : KSerializer<Dependency> {

            override val descriptor = PrimitiveSerialDescriptor("Dependency", PrimitiveKind.STRING)

            override fun deserialize(decoder: Decoder): Dependency {
                val split = decoder.decodeString().split("@").toMutableList()
                val name = split.first()
                split.removeFirst()
                val version = if (split.isEmpty()) null else split.joinToString(separator = "@")
                return Dependency(name, version?.let { parseVersionRange(it) })
            }

            override fun serialize(encoder: Encoder, value: Dependency) {
                encoder.encodeString("${value.name}@${value.versionRange}")
            }

        }

    }

}
