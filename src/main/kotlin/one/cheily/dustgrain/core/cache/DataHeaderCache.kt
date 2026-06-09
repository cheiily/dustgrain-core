package one.cheily.dustgrain.core.cache

import one.cheily.dustgrain.core.Application
import one.cheily.dustgrain.core.config.AppConfig
import one.cheily.dustgrain.core.domain.DataHeader
import one.cheily.dustgrain.core.fetching.DataFetchService
import net.harawata.appdirs.AppDirsFactory
import java.nio.file.Path

typealias DataHeaderCache = SuspendingKVCache<String, List<DataHeader>>

class InMemoryDataHeaderCache(
    dataFetchService: DataFetchService,
    appConfig: AppConfig = Application.config
) : InMemoryKVCache<String, List<DataHeader>>(
    provider = SuspendingCacheEntryProvider(dataFetchService::getTableHeaders),
    maxAgeSeconds = appConfig.cache.headers.maxAgeSeconds
)

class PersistentDataHeaderCache(
    dataFetchService: DataFetchService,
    appConfig: AppConfig = Application.config
) : PersistentKVCache<String, List<DataHeader>>(
    directory = Path.of(AppDirsFactory.getInstance().getUserCacheDir(
        appConfig.appInfo.name,
        appConfig.appInfo.version,
        appConfig.appInfo.author
    )).resolve("headers"),
    provider = SuspendingCacheEntryProvider(dataFetchService::getTableHeaders),
    keyCodec = StringCodec(),
    valueCodec = DataHeaderListCodec(),
    version = appConfig.cache.headers.version,
    maxAgeSeconds = appConfig.cache.headers.maxAgeSeconds
)

class NoopDataHeaderCache(
    dataFetchService: DataFetchService
) : NoopKVCache<String, List<DataHeader>>(
    provider = SuspendingCacheEntryProvider(dataFetchService::getTableHeaders)
)
