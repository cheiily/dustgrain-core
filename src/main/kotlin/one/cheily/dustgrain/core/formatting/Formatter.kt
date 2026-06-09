package one.cheily.dustgrain.core.formatting

import one.cheily.dustgrain.core.domain.DataField
import one.cheily.dustgrain.core.domain.DataGrain

@FunctionalInterface
fun interface Formatter {
    fun format(data: DataField): DataGrain
}