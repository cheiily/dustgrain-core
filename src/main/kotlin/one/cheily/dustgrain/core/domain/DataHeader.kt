package one.cheily.dustgrain.core.domain

import kotlinx.serialization.Serializable
import one.cheily.dustgrain.core.formatting.FormatterRef

@Serializable
data class DataHeader(
    val name: String,
    val type: FormatterRef,
    val delimiter: String?
) {
    val nameInResponse get() = name.replace("_", " ")
}
