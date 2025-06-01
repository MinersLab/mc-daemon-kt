package minerslab.mcd.launcher

import minerslab.mcd.McDaemon
import minerslab.mcd.api.command.Commands
import minerslab.mcd.launcher.command.HelpCommand
import minerslab.mcd.launcher.command.HereCommand
import minerslab.mcd.launcher.command.McDaemonCommand
import minerslab.mcd.launcher.command.SudoCommand
import java.nio.file.Path
import kotlin.io.path.div

fun main(vararg args: String) {
    val daemon = McDaemon(args, Path.of(System.getProperty("user.dir")) / ".mcd")
    Commands.defaultCommands.addAll(listOf(HelpCommand, McDaemonCommand, SudoCommand, HereCommand))
    daemon.start()
}
