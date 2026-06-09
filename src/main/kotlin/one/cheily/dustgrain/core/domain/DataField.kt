package one.cheily.dustgrain.core.domain

data class DataField(
    val content: String,
    val header: DataHeader
)

data class DataStruct(
    val fields: List<DataField>,
    val structureName: String?
)