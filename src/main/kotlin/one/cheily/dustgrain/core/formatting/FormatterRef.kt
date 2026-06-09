package one.cheily.dustgrain.core.formatting

import kotlinx.serialization.Serializable

@Serializable
enum class FormatterRef(
    var jsonType: String
) {
    WIKITEXT("wikitext"),
    IMAGE("file"),
    PASS("string"),
    PASS_ERROR("");

    companion object {
        fun find(type: String) = entries.firstOrNull {
            it.jsonType == type
        } ?: entries.firstOrNull {
            it.jsonType.contains(type, ignoreCase = true)
        } ?: entries.firstOrNull {
            type.contains(it.jsonType, ignoreCase = true)
        } ?: PASS_ERROR
    }
}