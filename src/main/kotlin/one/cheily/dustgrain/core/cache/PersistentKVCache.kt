package one.cheily.dustgrain.core.cache

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

open class PersistentKVCache<K, V>(
    val directory: Path,
    override val provider: SuspendingCacheEntryProvider<K, V>,
    val keyCodec: Codec<K, String>,
    val valueCodec: Codec<V, String>,
    val version: Int,
    val maxAgeSeconds: Long
) : SuspendingKVCache<K, V> {
    private val logger = KotlinLogging.logger {}

    private val cache = EntryPersistor(
        directory = directory,
        version = version
    )

    override fun get(key: K): V? =
        cache.get(keyCodec.encode(key)).let { result ->
            if (!result.success) {
                logger.error { "Couldn't read cache: $result" }
                null
            } else if (result.age == null || result.age > maxAgeSeconds) {
                invalidate(key)
                null
            } else {
                result.content
            }
        }?.let {
            valueCodec.decode(it)
        }

    override fun set(key: K, value: V) {
        cache.put(
            keyCodec.encode(key),
            valueCodec.encode(value)
        ).let { result ->
            if (!result.success) {
                logger.error { "Couldn't write cache: $result" }
            }
        }
    }

    override fun invalidate(key: K) {
        cache.remove(keyCodec.encode(key))
    }

    override fun clear() {
        cache.clear().let { result ->
            if (!result.success) {
                logger.error { "Couldn't clear cache: $result" }
            }
        }
    }
}