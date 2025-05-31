package minerslab.mcd.api.permission

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import minerslab.mcd.api.command.ServerCommandSource

/**
 * 要求命令调用者拥有某些权限
 */
fun permission(permission: String): ServerCommandSource.() -> Boolean =
    permission(permission) { !(it == null || (it is JsonPrimitive && (it.doubleOrNull == 0.0 || it.booleanOrNull == false || it.content.isEmpty()))) }

/**
 * 对命令调用者拥有权限进行判断
 */
fun permission(permission: String, predicate: (JsonElement?) -> Boolean): ServerCommandSource.() -> Boolean = {
    predicate(McDaemonPermissionApi.instance.getUserPermission(sender, permission))
}
