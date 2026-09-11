package one.cheily.dustgrain.core

import io.ktor.client.*
import one.cheily.dustgrain.core.cache.*
import one.cheily.dustgrain.core.config.AppConfig
import one.cheily.dustgrain.core.config.AppProfile
import one.cheily.dustgrain.core.config.getHttpClient
import one.cheily.dustgrain.core.config.loadConfig
import one.cheily.dustgrain.core.fetching.DataFetchService
import one.cheily.dustgrain.core.formatting.FormattingService
import one.cheily.dustgrain.core.gamemodule.GameModules

object Application {
    lateinit var profile: AppProfile private set

    lateinit var config: AppConfig private set
    lateinit var httpClient: HttpClient private set
    lateinit var dataFetchService: DataFetchService private set
    lateinit var dataHeaderCache: DataHeaderCache private set
    lateinit var formattingService: FormattingService private set
    lateinit var gameModules: GameModules private set

    @JvmOverloads
    fun initialize(profile: AppProfile, appConfig: AppConfig = loadConfig()) {
        this.profile = profile
        this.config = appConfig

        this.httpClient = getHttpClient()
        this.dataFetchService = DataFetchService()
        this.dataHeaderCache = when (appConfig.cache.headers.mode) {
            CacheMode.IN_MEMORY -> InMemoryDataHeaderCache(dataFetchService)
            CacheMode.PERSISTENT -> PersistentDataHeaderCache(dataFetchService)
            CacheMode.NOOP -> NoopDataHeaderCache(dataFetchService)
        }
        this.formattingService = FormattingService()
        this.gameModules = GameModules()
    }
}
