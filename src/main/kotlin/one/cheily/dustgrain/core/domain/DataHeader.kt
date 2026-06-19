package one.cheily.dustgrain.core.domain

import one.cheily.dustgrain.core.formatting.FormatterRef
import kotlinx.serialization.Serializable

@Serializable
data class DataHeader(
    val name: String,
    val type: FormatterRef,
    val delimiter: String?
) {
    val nameInResponse get() = name.replace("_", " ")
}
