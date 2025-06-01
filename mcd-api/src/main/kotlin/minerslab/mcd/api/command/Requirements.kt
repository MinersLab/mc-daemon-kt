package minerslab.mcd.api.command

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

fun requirement(block: ServerCommandSource.() -> Boolean): Requirement = block

operator fun Requirement.not(): Requirement = { !this@not.invoke(this) }
infix fun Requirement.or(other: Requirement): Requirement = { this@or.invoke(this) || other.invoke(this) }
infix fun Requirement.and(other: Requirement): Requirement = { this@and.invoke(this) && other.invoke(this) }
