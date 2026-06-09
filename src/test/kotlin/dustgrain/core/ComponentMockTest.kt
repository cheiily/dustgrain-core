package dustgrain.core

import one.cheily.dustgrain.core.config.AppConfig
import one.cheily.dustgrain.core.config.AppProfile
import one.cheily.dustgrain.core.config.getHttpClient
import one.cheily.dustgrain.core.fetching.DataFetchService
import one.cheily.dustgrain.core.fetching.DustloopClient
import one.cheily.dustgrain.core.formatting.FormattingService
import io.kotest.core.spec.style.FeatureSpec
import io.ktor.client.plugins.defaultRequest
import java.net.URI
import one.cheily.dustgrain.core.cache.CacheMode

abstract class ComponentMockTest(body: ComponentMockTest.() -> Unit = {}) : FeatureSpec({}) {
    init {
        body()
    }

    open val mockUrl = "http://localhost:12345"

    val mockConfig by lazy {
        AppConfig(
            appInfo = AppConfig.AppInfo(
                name = "test-app",
                version = "0.1.0",
                author = "test-author"
            ),
            cache = AppConfig.Cache(
                headers = AppConfig.CacheConf(
                    version = 1,
                    maxAgeSeconds = 3600L,
                    mode = CacheMode.NOOP
                )
            ),
            client = AppConfig.Client(
                url = URI.create(mockUrl).toURL(),
                timeout = 1000L,
                userAgent = "test-agent"),
            cargoQueries = emptyList()
        )
    }

    val mockClient by lazy {
        getHttpClient(
            appProfile = AppProfile.CLI,
            config = mockConfig
        ).config {
            defaultRequest {
                url("$mockUrl?format=json")
            }
        }
    }

    val mockDustloopClient by lazy {
        DustloopClient(client = mockClient)
    }

    val mockDataFetchService by lazy {
        DataFetchService(client = mockDustloopClient)
    }

    val mockDataHeaderCache: one.cheily.dustgrain.core.cache.DataHeaderCache by lazy {
        one.cheily.dustgrain.core.cache.InMemoryDataHeaderCache(
            dataFetchService = mockDataFetchService,
            appConfig = mockConfig
        )
    }

    val mockFormattingService by lazy {
        FormattingService(dataFetchService = mockDataFetchService)
    }
}
