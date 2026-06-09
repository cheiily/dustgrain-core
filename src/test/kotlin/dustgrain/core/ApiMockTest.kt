package dustgrain.core

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.Spec
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener

abstract class ApiMockTest(body: ApiMockTest.() -> Unit = {}) : ComponentMockTest() {
    val logger = KotlinLogging.logger {}
    val wiremockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().port(12345))

    init {
        extension(WireMockListener(
            wiremockServer,
            ListenerMode.PER_TEST
        ))

        body()
    }

    override val mockUrl: String by lazy { wiremockServer.baseUrl() }

    override suspend fun beforeSpec(spec: Spec) {
        wiremockServer.start()
    }

    override suspend fun afterSpec(spec: Spec) {
        wiremockServer.stop()
    }

    fun thereAreCargoTables(directory: String = "fetching") {
        val resourcePath = "/dustgrain/core/$directory/cargotables.json"
        val content = javaClass.getResource(resourcePath)?.readText()
            ?: throw RuntimeException("Resource not found: $resourcePath")

        wiremockServer.stubFor(
            get(urlPathMatching("/.*"))
                .withQueryParam("action", equalTo("cargotables"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(content)
                )
        )
    }

    fun thereAreCargoFields(tableName: String, directory: String = "fetching") {
        val resourcePath = "/dustgrain/core/$directory/cargofields_$tableName.json"
        val content = javaClass.getResource(resourcePath)?.readText()
            ?: throw RuntimeException("Resource not found: $resourcePath")

        wiremockServer.stubFor(
            get(urlPathMatching("/.*"))
                .withQueryParam("action", equalTo("cargofields"))
                .withQueryParam("table", equalTo(tableName))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(content)
                )
        )
    }

    fun thereIsAMediaWikiError(variant: String) {
        val resourcePath = "/dustgrain/core/fetching/error_response_$variant.json"
        val content = javaClass.getResource(resourcePath)?.readText()
            ?: throw RuntimeException("Resource not found: $resourcePath")

        wiremockServer.stubFor(
            get(urlPathMatching("/.*"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(content)
                )
        )
    }

    fun thereIsACargoQueryResult(variant: String, directory: String = "fetching") {
        val resourcePath = "/dustgrain/core/$directory/cargoquery_$variant.json"
        val content = javaClass.getResource(resourcePath)?.readText()
            ?: throw RuntimeException("Resource not found: $resourcePath")

        wiremockServer.stubFor(
            get(urlPathMatching("/.*"))
                .withQueryParam("action", equalTo("cargoquery"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(content)
                )
        )
    }

    fun thereIsImageData(variant: String) {
        val resourcePath = "/dustgrain/core/fetching/imagedata_$variant.json"
        val content = javaClass.getResource(resourcePath)?.readText()
            ?: throw RuntimeException("Resource not found: $resourcePath")

        wiremockServer.stubFor(
            get(urlPathMatching("/.*"))
                .withQueryParam("action", equalTo("query"))
                .withQueryParam("prop", equalTo("imageinfo"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(content)
                )
        )
    }
}
