package minerslab.mcd.api.permission

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import minerslab.mcd.api.command.ServerCommandSource


fun permission(permission: String): ServerCommandSource.() -> Boolean = permission(permission) { !(it == null || (it is JsonPrimitive && it.doubleOrNull == 0.0)) }
fun permission(permission: String, predicate: (JsonElement?) -> Boolean): ServerCommandSource.() -> Boolean = {
    predicate(McDaemonPermissionApi.instance.getUserPermission(sender, permission))
}
