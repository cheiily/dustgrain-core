package one.cheily.dustgrain.core.fetching

import io.github.oshai.kotlinlogging.KotlinLogging
import one.cheily.dustgrain.core.Application
import one.cheily.dustgrain.core.domain.DataHeader
import one.cheily.dustgrain.core.formatting.FormatterRef
import kotlinx.coroutines.runBlocking

class DataFetchService(
    var client: DustloopClient = DustloopClient(Application.httpClient)
) {
    val logger = KotlinLogging.logger{}

    suspend fun getTableList(): List<String> = client.getTableList().cargotables

    fun getTableListBlocking() = runBlocking { getTableList() }

    suspend fun getTableHeaders(tableName: String): List<DataHeader> = client
        .getTableHeaders(tableName)
        .cargofields
        .map { (field, fieldData) ->
            DataHeader(
                name = field.name,
                type = FormatterRef.find(fieldData.type),
                delimiter = fieldData.delimiter
            ).let {
                if (it.type == FormatterRef.PASS_ERROR) {
                    logger.warn { "Unknown cargo type - \"${fieldData.type}\", for header ${it.name}!" }
                }
                it
            }
        }

    fun getTableHeadersBlocking(tableName: String) = runBlocking { getTableHeaders(tableName) }

    suspend fun getTableData(query: TableDataRequest): List<Map<String, String?>> = client
        .getTableData(query)
        .cargoquery.map { it.data }

    fun getTableDataBlocking(query: TableDataRequest) = runBlocking { getTableData(query) }

    suspend fun getImageInfo(imageName: String): ImageDataResponse.ImageUrlQuery.ImagePage.ImageInfo = client
        .getImageData(imageName)
        .query.pages.first()
        .imageinfo.first()

    fun getImageInfoBlocking(imageName: String) = runBlocking { getImageInfo(imageName) }

    suspend fun getImageUrl(imageName: String): String = client
        .getImageData(imageName)
        .query.pages.first()
        .imageinfo.first()
        .url

    fun getImageUrlBlocking(imageName: String) = runBlocking { getImageUrl(imageName) }
}