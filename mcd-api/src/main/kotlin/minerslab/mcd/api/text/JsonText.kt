package minerslab.mcd.api.text

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

private val json = Json {
    explicitNulls = true
}

@Serializable
data class JsonText(
    val text: String? = null,
    val extra: List<JsonText>? = null,
    val color: String? = null,
    @SerialName("shadow_color") val shadowColor: List<Double>? = null,
    val font: String? = null,
    val insertion: String? = null,

    val bold: Boolean? = null, val italic: Boolean? = null, val underlined: Boolean? = null, val strikethrough: Boolean? = null, val obfuscated: Boolean? = null,
    val translate: String? = null, val fallback: String? = null, val with: List<JsonText>? = null,
    val keybind: String? = null,
    val score: Score? = null,
    val separator: String? = null,
    val selector: String? = null,
    val nbt: String? = null, val interpret: Boolean? = null, val source: String? = null, val block: String? = null, val entity: String? = null, val storage: String? = null,
    @SerialName("click_event") val clickEvent: ClickEvent? = null,
    @SerialName("hover_event") val hoverEvent: HoverEvent? = null
) {

    @Serializable
    data class HoverEvent(
        val action: Action,
        val id: String? = null,
        val name: List<JsonText>? = null,
        val uuid: String? = null,
        val components: JsonObject? = null,
        val count: Int? = null,
        val value: List<JsonText>? = null
    ) {

        @Serializable
        enum class Action {
            @SerialName("show_entity") SHOW_ENTITY,
            @SerialName("show_item") SHOW_ITEM,
            @SerialName("show_text") SHOW_TEXT;
        }

    }

    @Serializable
    data class ClickEvent(
        val action: Action,
        val page: Int? = null,
        val value: String? = null,
        val id: String? = null, val payload: String? = null,
        val path: String? = null,
        val url: String? = null,
        val command: String? = null,
        val dialog: JsonElement? = null
    ) {

        @Serializable
        enum class Action {
            @SerialName("change_page") CHANGE_PAGE,
            @SerialName("copy_to_clipboard") COPY_TO_CLIPBOARD,
            @SerialName("custom") CUSTOM,
            @SerialName("open_file") OPEN_FILE,
            @SerialName("open_url") OPEN_URL,
            @SerialName("run_command") RUN_COMMAND,
            @SerialName("show_dialog") SHOW_DIALOG,
            @SerialName("suggest_command") SUGGEST_COMMAND;
        }

    }

    @Serializable
    data class Score(val name: String, val objective: String)

    fun asString() = json.encodeToString(this)

}

fun List<JsonText>.asString() = json.encodeToString(this)
