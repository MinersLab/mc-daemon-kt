package minerslab.mcd.api.permission

import kotlinx.serialization.json.JsonElement
import minerslab.mcd.api.McDaemonModule
import minerslab.mcd.api.config.useConfig
import minerslab.mcd.findModule
import minerslab.mcd.mcDaemon
import minerslab.mcd.util.Namespaces.MC_DAEMON
import starry.adventure.registry.Identifiers.div

class McDaemonPermissionApi : McDaemonModule {

    companion object

    val configWrapper = useConfig<PermissionConfig>(MC_DAEMON / "permissions.json")
    var config by configWrapper

    fun addGroupExtends(group: String, vararg extends: String) {
        config = config.apply { groups[group]?.extends += extends }
    }

    fun removeGroupExtends(group: String, vararg extends: String) {
        config = config.apply { groups[group]?.extends -= extends }
    }

    fun addGroupPermissions(group: String, vararg permissions: Pair<String, JsonElement>) {
        config = config.apply { groups[group]?.permissions += permissions }
    }

    fun removeGroupPermissions(group: String, vararg permissions: String) {
        config = config.apply { groups[group]?.permissions -= permissions }
    }

    fun createGroup(group: String) {
        config = config.apply { groups.putIfAbsent(group, PermissionConfig.Group()) }
    }

    fun deleteGroup(group: String) {
        config = config.apply { groups -= group }
    }

    fun createUser(user: String) {
        config = config.apply { users.putIfAbsent(user, PermissionConfig.User()) }
    }

    fun deleteUser(user: String) {
        config = config.apply { users -= user }
    }

    fun addUserGroups(user: String, vararg groups: String) {
        config = config.apply { users.putIfAbsent(user, PermissionConfig.User())?.groups += groups }
    }

    fun removeUserGroups(user: String, vararg groups: String) {
        config = config.apply { users.putIfAbsent(user, PermissionConfig.User())?.groups -= groups }
    }

    fun addUserPermissions(user: String, vararg permissions: Pair<String, JsonElement>) {
        config = config.apply { users.putIfAbsent(user, PermissionConfig.User())?.permissions += permissions }
    }

    fun removeUserPermissions(user: String, vararg permissions: String) {
        config = config.apply { users.putIfAbsent(user, PermissionConfig.User())?.permissions -= permissions }
    }

    fun getUserPermission(user: String, permission: String) = getUserPermissions(user)?.get(permission)
    fun getGroupPermission(group: String, permission: String) = getGroupPermissions(group)?.get(permission)

    fun getUserPermissions(user: String): Map<String, JsonElement>? {
        val user = config.users[user] ?: return null
        val userPerms = mutableMapOf<String, JsonElement>()
        for (group in user.groups) userPerms.putAll(getGroupPermissions(group) ?: continue)
        userPerms.putAll(user.permissions)
        return userPerms
    }

    fun getGroupPermissions(group: String): Map<String, JsonElement>? {
        val group = config.groups[group] ?: return null
        val groupPerms = mutableMapOf<String, JsonElement>()
        for (parent in group.extends) groupPerms.putAll(getGroupPermissions(parent) ?: continue)
        groupPerms.putAll(group.permissions)
        return groupPerms
    }

}

val McDaemonPermissionApi.Companion.instance
    get() = mcDaemon.findModule<McDaemonPermissionApi>()
