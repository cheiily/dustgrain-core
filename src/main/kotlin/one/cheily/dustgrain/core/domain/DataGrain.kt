package one.cheily.dustgrain.core.domain

data class DataGrain(
    val contents: List<String>,
    val header: DataHeader
)

data class DataSpike(
    val grains: List<DataGrain>,
    val structureName: String?
)