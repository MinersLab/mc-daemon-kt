package minerslab.mcd.api.command

import minerslab.mcd.api.McDaemonApi
import minerslab.mcd.api.instance
import minerslab.mcd.api.config.FeatureConfig
import minerslab.mcd.api.config.isFutureEnabled

/**
 * 命令需求
 */
typealias Requirement = (ServerCommandSource.() -> Boolean)

/**
 * 要求命令的调用者必须为玩家
 */
object IsPlayer : Requirement {
    override fun invoke(source: ServerCommandSource) = !source.isServer
}

/**
 * 要求命令的调用者必须为服务器
 */
object IsServer : Requirement {
    override fun invoke(source: ServerCommandSource) = source.isServer
}


/**
 * 要求特性必须被启用
 * @param name 特性名称
 * @param defaultEnabled 当传入值为 `false`，根据 [FeatureConfig.enabled] 判断；当传入值为 `true`，根据 [FeatureConfig.disabled] 判断
 */
fun feature(name: String, defaultEnabled: Boolean = true): Requirement = {
    McDaemonApi.instance.features.isFutureEnabled(name, defaultEnabled)
}

operator fun Requirement.not(): Requirement = { !this@not.invoke(this) }
infix fun Requirement.or(other: Requirement): Requirement = { this@or.invoke(this) || other.invoke(this) }
infix fun Requirement.and(other: Requirement): Requirement = { this@and.invoke(this) && other.invoke(this) }
