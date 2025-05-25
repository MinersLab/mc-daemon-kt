package minerslab.mcd.api.text

import java.util.stream.IntStream

class TextBuilder : CharSequence {

    private var text = ""

    override fun subSequence(startIndex: Int, endIndex: Int) = text.subSequence(startIndex, endIndex)
    override fun chars(): IntStream = text.chars()
    override fun get(index: Int) = text[index]
    override val length: Int
        get() = text.length

    override fun codePoints(): IntStream = text.codePoints()
    override fun isEmpty() = text.isEmpty()
    override fun toString() = text

    fun a(textBuilder: TextBuilder) = also { text += textBuilder.text }
    fun a(string: String) = also { text += string }
    fun a(obj: Any) = also { text += obj }

    fun black() = a("§0")
    fun darkBlue() = a("§1")
    fun darkGreen() = a("§2")
    fun darkAqua() = a("§3")
    fun darkRed() = a("§4")
    fun darkPurple() = a("§5")
    fun gold() = a("§6")
    fun gray() = a("§7")
    fun darkGray() = a("§8")
    fun blue() = a("§9")
    fun green() = a("§a")
    fun aqua() = a("§b")
    fun red() = a("§c")
    fun lightPurple() = a("§d")
    fun yellow() = a("§e")
    fun white() = a("§f")

    fun obfuscated() = a("§k")
    fun bold() = a("§l")
    fun strikethrough() = a("§m")
    fun underlined() = a("§n")
    fun italic() = a("§o")
    fun reset() = a("§r")

    fun copy() = TextBuilder().a(text)

}

fun text() = TextBuilder()

fun TextBuilder.asJsonText() = JsonText(this.toString())
fun TextBuilder.asComponent() = asJsonText().asComponent()
