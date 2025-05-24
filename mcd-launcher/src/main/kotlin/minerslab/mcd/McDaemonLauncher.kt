package minerslab.mcd

import minerslab.mcd.api.command.Commands
import minerslab.mcd.command.HelpCommand
import minerslab.mcd.command.McDaemonCommand
import minerslab.mcd.command.SudoCommand
import java.nio.file.Path
import kotlin.io.path.div

fun main(vararg args: String) {
    Commands.defaultCommands.addAll(listOf(HelpCommand, McDaemonCommand, SudoCommand))
    val daemon = McDaemon(args, Path.of(System.getProperty("user.dir")) / ".mcd")
    daemon.start()
}
