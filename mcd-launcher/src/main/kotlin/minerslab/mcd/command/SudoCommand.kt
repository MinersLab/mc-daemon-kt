package minerslab.mcd.command

import com.mojang.brigadier.arguments.StringArgumentType
import minerslab.mcd.api.command.ServerCommandDispatcher
import minerslab.mcd.api.command.ServerRequirement
import minerslab.mcd.api.command.and
import minerslab.mcd.api.command.feature
import minerslab.mcd.api.command.or
import minerslab.mcd.api.permission.permission
import minerslab.mcd.util.Namespaces.MC_DAEMON
import starry.adventure.brigadier.command.argument
import starry.adventure.brigadier.dispatcher.register
import java.util.function.Consumer

object SudoCommand : Consumer<ServerCommandDispatcher> {

    override fun accept(t: ServerCommandDispatcher) = t.register {
        literal("sudo") {
            requires(feature("command.$MC_DAEMON.sudo") and (permission("command.$MC_DAEMON.sudo") or ServerRequirement))
            argument("sender", StringArgumentType.string()) {
                argument("command", StringArgumentType.greedyString()) {
                    execute {
                        val sender: String by argument()
                        val command: String by argument()
                        source.sender = sender
                        source.rawMessage = "${source.handler.config.server.daemonCommandPrefix}$command"
                        t.execute(command, source)
                    }
                }
            }
        }
    }

}