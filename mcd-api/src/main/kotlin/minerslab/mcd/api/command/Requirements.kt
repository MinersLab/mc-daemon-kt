package minerslab.mcd.api.command

import minerslab.mcd.api.McDaemonApi
import minerslab.mcd.api.instance

typealias Requirement = (ServerCommandSource.() -> Boolean)

object PlayerRequirement : Requirement {
    override fun invoke(p1: ServerCommandSource) = !p1.isServer
}

object ServerRequirement : Requirement {
    override fun invoke(p1: ServerCommandSource) = p1.isServer
}

fun feature(name: String, defaultEnabled: Boolean = true): Requirement = {
    if (defaultEnabled) name !in McDaemonApi.instance.featureConfig.disabled
    else name in McDaemonApi.instance.featureConfig.enabled
}

operator fun Requirement.not(): Requirement = { !this@not.invoke(this) }
infix fun Requirement.or(other: Requirement): Requirement = { this@or.invoke(this) || other.invoke(this) }
infix fun Requirement.and(other: Requirement): Requirement = { this@and.invoke(this) && other.invoke(this) }
