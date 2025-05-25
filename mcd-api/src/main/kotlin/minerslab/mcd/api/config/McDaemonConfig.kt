package minerslab.mcd.api.config

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import minerslab.mcd.mcDaemon
import starry.adventure.core.registry.Identifier
import java.io.File
import kotlin.io.path.div
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.createType

object McDaemonConfig {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        prettyPrintIndent = " ".repeat(4)
    }

}

@Suppress("UNCHECKED_CAST")
class WrappedConfig<T : Any>(val file: File, configClass: KClass<T>, val format: StringFormat) {

    private val serializer = serializer(configClass.createType()) as KSerializer<T>
    private var value = load()

    fun load() = format.decodeFromString(serializer, file.readText())
    fun reload() { value = load() }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = value

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        file.writeText(format.encodeToString(serializer, value))
    }

}
inline fun <reified T : Any> useConfig(identifier: Identifier, format: StringFormat = McDaemonConfig.json) = useConfig<T>("${identifier.getNamespace()}/${identifier.getPath()}", format)

inline fun <reified T : Any> useConfig(path: String, format: StringFormat = McDaemonConfig.json) =
    (mcDaemon.path / "config" / path).toFile().apply {
        parentFile.mkdirs()
        if (!isFile) {
            createNewFile()
            writeText(format.encodeToString<T>(Json.decodeFromString("{}")))
        }
    }.let { WrappedConfig(it, T::class, format) }