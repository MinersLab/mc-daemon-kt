package minerslab.mcd.api.permission

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PermissionConfig(
    val groups: MutableMap<String, Group> = mutableMapOf(),
    val users: MutableMap<String, User> = mutableMapOf()
) {

    @Serializable
    data class Group(val permissions: MutableMap<String, JsonElement> = mutableMapOf())

    @Serializable
    data class User(val groups: MutableSet<String> = mutableSetOf(), val permissions: MutableMap<String, JsonElement> = mutableMapOf())

}
