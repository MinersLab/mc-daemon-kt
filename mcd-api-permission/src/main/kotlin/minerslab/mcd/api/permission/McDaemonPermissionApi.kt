package minerslab.mcd.api.permission

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import minerslab.mcd.McDaemon
import minerslab.mcd.api.McDaemonApi
import minerslab.mcd.api.McDaemonModule
import minerslab.mcd.api.api
import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.findModule
import kotlin.io.path.div

class McDaemonPermissionApi : McDaemonModule {

    private lateinit var daemon: McDaemon

    private val configFile by lazy {
        (daemon.path / "config" / "mcd" / "permissions.json").toFile().apply {
            parentFile.mkdirs()
            if (!isFile) {
                createNewFile()
                writeText(Json.encodeToString(PermissionConfig()))
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        prettyPrint = true
        prettyPrintIndent = " ".repeat(4)
    }

    var config: PermissionConfig
        get() = json.decodeFromString(configFile.readText())
        set(value) {
            configFile.writeText(json.encodeToString(configFile))
        }

    override fun start(daemon: McDaemon) {
        this.daemon = daemon
    }

    fun getPermission(user: String, permission: String): JsonElement? {
        val user = config.users[user] ?: return null
        val permissions = mutableMapOf<String, JsonElement>()
        permissions.putAll(user.permissions)
        permissions.putAll(user.groups.mapNotNull { config.groups[it]?.permissions }.flatMap { it.entries }.associate { it.key to it.value })
        return permissions[permission]
    }

}

fun permission(permission: String): ServerCommandSource.() -> Boolean = permission(permission) { !(it == null || (it is JsonPrimitive && it.doubleOrNull == 0.0)) }
fun permission(permission: String, predicate: (JsonElement?) -> Boolean): ServerCommandSource.() -> Boolean = {
    predicate(daemon.api.permission.getPermission(sender, permission))
}

val McDaemonApi.permission
    get() = this.daemon.findModule<McDaemonPermissionApi>()
