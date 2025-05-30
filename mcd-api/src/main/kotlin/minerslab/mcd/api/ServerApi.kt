package minerslab.mcd.api

import minerslab.mcd.api.command.ServerCommandSource
import minerslab.mcd.api.text.JsonText
import minerslab.mcd.api.text.asString
import minerslab.mcd.handler.CommandExecutingMode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import starry.adventure.core.util.Wrapped
import starry.adventure.core.util.wrapped

/**
 * 以服务器身份执行命令
 * @param command 命令文本
 * @see [minerslab.mcd.handler.ServerHandler.command]
 */
fun ServerCommandSource.command(command: String, mode: CommandExecutingMode = CommandExecutingMode.RCON) = handler.command(command, mode)

/**
 * 发送命令反馈
 * @param raw 包装后的原始 tellraw 文本参数
 * @param broadcast 是否广播到全服
 */
fun ServerCommandSource.sendFeedback(raw: Wrapped<String>, broadcast: Boolean = false) = handler.run {
    if (!isServer || broadcast) command(
        handler.getCommandHelper().tellraw(
            if (broadcast) "@a" else sender,
            raw.unwrap()
        )
    )
    if (isServer || broadcast) {
        val json = JSONComponentSerializer.json().deserialize(raw.unwrap())
        val ansi = ANSIComponentSerializer.ansi().serialize(json)
        val legacy = LegacyComponentSerializer.legacySection().deserialize(ansi)
        val colored = ANSIComponentSerializer.ansi().serialize(legacy)
        println(colored)
    }
}

/**
 * 发送命令反馈
 * @param content [JsonText] 列表， 拼接多个 [JsonText]
 * @param broadcast 是否广播到全服
 */
fun ServerCommandSource.sendFeedback(content: List<JsonText>, broadcast: Boolean = false) =
    sendFeedback(content.asString().wrapped(), broadcast)

/**
 * 发送命令反馈
 * @param content 将要发送的 [JsonText] 文本
 * @param broadcast 是否广播到全服
 */
fun ServerCommandSource.sendFeedback(content: JsonText, broadcast: Boolean = false) =
    sendFeedback(listOf(content), broadcast)

/**
 * 发送命令反馈
 * @param content 传统聊天文本
 * @param broadcast 是否广播到全服
 */
fun ServerCommandSource.sendFeedback(content: CharSequence, broadcast: Boolean = false) =
    sendFeedback(JsonText(text = String(content.toList().toCharArray())), broadcast)

/**
 * 发送命令反馈
 * @param content 文本组件
 * @param broadcast 是否广播到全服
 */
fun ServerCommandSource.sendFeedback(content: Component, broadcast: Boolean = false) =
    sendFeedback(JSONComponentSerializer.json().serialize(content).wrapped(), broadcast)


