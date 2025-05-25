package minerslab.mcd

import minerslab.mcd.api.command.Commands
import minerslab.mcd.command.HelpCommand
import minerslab.mcd.command.HereCommand
import minerslab.mcd.command.McDaemonCommand
import minerslab.mcd.command.SudoCommand
import java.nio.file.Path
import kotlin.io.path.div

fun main(vararg args: String) {
    val daemon = McDaemon(args, Path.of(System.getProperty("user.dir")) / ".mcd")
    Commands.defaultCommands.addAll(listOf(HelpCommand, McDaemonCommand, SudoCommand, HereCommand))
    daemon.start()
}
