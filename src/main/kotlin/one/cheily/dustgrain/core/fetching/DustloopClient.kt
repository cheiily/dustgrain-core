package one.cheily.dustgrain.core.fetching

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import one.cheily.dustgrain.core.DustloopApiException

class DustloopClient(
    val client: HttpClient
) {
    suspend fun getTableList(): TableListResponse = try {
        client.get {
            parameter("action", "cargotables")
        }.body()
    } catch (e: Throwable) {
        throw DustloopApiException("Couldn't fetch table list.", e)
    }

    suspend fun getTableHeaders(tableName: String): TableHeaderResponse = try {
        client.get {
            parameter("action", "cargofields")
            parameter("table", tableName)
        }.body()
    } catch (e: Throwable) {
        throw DustloopApiException("Couldn't fetch table headers.", e)
    }

    suspend fun getTableData(request: TableDataRequest): TableDataResponse = try {
        client.get {
            parameter("action", "cargoquery")
            parameter("tables", request.tables.joinToString(","))
            parameter("fields", request.fields.joinToString(","))
            request.where?.let { parameter("where", it) }
            request.joinOn?.let { parameter("join_on", it) }
            request.groupBy?.let { parameter("group_by", it) }
            request.having?.let { parameter("having", it) }
            request.orderBy?.let { parameter("order_by", it) }
            request.limit?.let { parameter("limit", it) }
            request.offset?.let { parameter("offset", it) }
        }.body()
    } catch (e: Throwable) {
        throw DustloopApiException("Couldn't fetch table data.", e)
    }

    suspend fun getImageData(imageName: String): ImageDataResponse = try {
        client.get {
            val fileParam = if (imageName.startsWith("File:")) imageName else "File:$imageName"
            parameter("action", "query")
            parameter("prop", "imageinfo")
            parameter("iiprop", "url|size|mime")
            parameter("titles", fileParam)
            parameter("formatversion", 2)
        }.body()
    } catch (e: Throwable) {
        throw DustloopApiException("Couldn't fetch image data.", e)
    }
}