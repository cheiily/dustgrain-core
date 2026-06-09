package one.cheily.dustgrain.core.formatting

import one.cheily.dustgrain.core.domain.DataField
import one.cheily.dustgrain.core.domain.DataGrain
import one.cheily.dustgrain.core.domain.DataHeader
import one.cheily.dustgrain.core.fetching.DataFetchService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import one.cheily.dustgrain.core.Application
import one.cheily.dustgrain.core.domain.DataSpike
import one.cheily.dustgrain.core.domain.DataStruct

class FormattingService(
    val dataFetchService: DataFetchService = Application.dataFetchService
) {
    val logger = KotlinLogging.logger{}


    fun format(data: List<DataField>): List<DataGrain> =
        data.map {
            matchFormat(it.header)
                .format(it)
        }

    fun format(data: DataStruct) = DataSpike(
        structureName = data.structureName,
        grains = data.fields.map { format(it) }
    )
    fun format(data: DataField): DataGrain = matchFormat(data.header).format(data)


    // ========== Formatter Implementations ==========
    val formatPass = Formatter { data ->
        DataGrain(
            header = data.header,
            contents = data.parseList(data.content)
        )
    }

    val formatErrorPass = Formatter { data ->
        logger.warn { "Unknown cargo type in formatting! Header: ${data.header}" }
        DataGrain(
            header = data.header,
            contents = listOf(data.content)
        )
    }

    val formatImage = Formatter { data ->
        val filenames = data.parseList(data.content)
        val fileUrls = runBlocking {
                filenames.map { async {
                    dataFetchService.getImageUrl(it)
                }}.awaitAll()
            }

        DataGrain(
            header = data.header,
            contents = fileUrls
        )
    }

    val formatWikitext = Formatter { data ->
        logger.warn { "wikitext formatting is a TODO feature" }
        formatPass.format(data)
//        TODO("See issue #14")
    }


    // ========== Misc ==========
    fun matchFormat(header: DataHeader): Formatter =
        when (header.type) {
            FormatterRef.IMAGE -> formatImage
            FormatterRef.WIKITEXT -> formatWikitext
            FormatterRef.PASS -> formatPass
            FormatterRef.PASS_ERROR -> formatErrorPass
        }


    private fun DataField.parseList(content: String): List<String> =
        if (header.delimiter == null)
            listOf(content)
        else
            content.split(header.delimiter)

}

