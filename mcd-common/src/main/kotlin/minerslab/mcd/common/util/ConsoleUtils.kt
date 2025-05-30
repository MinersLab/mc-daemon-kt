package minerslab.mcd.common.util

import com.mojang.brigadier.suggestion.Suggestion
import minerslab.mcd.api.command.Commands
import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.handler.ServerHandler
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.DefaultParser
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

fun ServerHandler<*>.createConsoleReader(terminalConfiguration: (TerminalBuilder) -> Unit = {}): LineReaderBuilder {
    val terminal: Terminal = TerminalBuilder.builder()
        .system(true)
        .jansi(true)
        .also(terminalConfiguration)
        .build()

    val completer = Completer { reader, line, candidates ->
        val text = line.line()
        if (text.isBlank()) {
            candidates.add(Candidate("/"))
        }
        if (text.startsWith(config.server.gameCommandPrefix)) return@Completer
        val cursor = line.cursor()
        val source = ServerCommandSource(this, "Server", text, true)
        val dispatcher = Commands.getDispatcher()
        val parsed = dispatcher.parse(text, source)
            ?: return@Completer
        val suggestions = dispatcher.getCompletionSuggestions(parsed, cursor)
            .get()
        suggestions
            .list
            .map(Suggestion::getText)
            .map(::Candidate)
            .forEach(candidates::add)
    }

    val reader = LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(completer)
        .parser(
            object : DefaultParser() {
                override fun isEscapeChar(ch: Char) = false
            }
        )
        //.option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
    return reader
}