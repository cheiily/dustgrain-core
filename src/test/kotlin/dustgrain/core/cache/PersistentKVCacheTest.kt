package dustgrain.core.cache

import dustgrain.core.deleteRecursively
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.engine.concurrency.TestExecutionMode
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import one.cheily.dustgrain.core.cache.PersistentKVCache
import one.cheily.dustgrain.core.cache.StringCodec
import one.cheily.dustgrain.core.cache.SuspendingCacheEntryProvider
import java.io.File
import java.nio.file.Path

class PersistentKVCacheTest : FeatureSpec({

    val keyCodec = StringCodec()
    val valueCodec = StringCodec()

    fun someProvider(): SuspendingCacheEntryProvider<String, String> {
        return SuspendingCacheEntryProvider { key -> "loaded value for $key" }
    }

    lateinit var tempDir : File
    fun getCache() = PersistentKVCache(Path.of(tempDir.path), someProvider(), keyCodec, valueCodec, 1, 3)
    lateinit var cache : PersistentKVCache<String, String>

    beforeEach {
        tempDir = tempdir()
        cache = getCache()
    }

    afterEach {
        cache.clear()
        tempDir.toPath().deleteRecursively()
    }

    feature("get and set") {
        scenario("should return a value that has been set") {
            // given
            cache

            // when
            cache.set("key1", "value1")
            val value = cache.get("key1")

            // then
            value shouldBe "value1"
        }

        scenario("should return null for a key that has not been set") {
            // given
            cache

            // when
            val value = cache.get("key1")

            // then
            value.shouldBeNull()
        }
    }

    feature("invalidate") {
        scenario("should remove a value from the cache") {
            // given
            cache.set("key1", "value1")

            // when
            cache.invalidate("key1")
            val value = cache.get("key1")

            // then
            value.shouldBeNull()
        }
    }

    feature("clear") {
        scenario("should remove all values from the cache") {
            // given
            cache.set("key1", "value1")
            cache.set("key2", "value2")

            // when
            cache.clear()

            // then
            cache.get("key1").shouldBeNull()
            cache.get("key2").shouldBeNull()
        }
    }

    feature("persistence") {
        scenario("should persist data between cache instances") {
            // given
            val cache1 = PersistentKVCache(Path.of(tempDir.path), someProvider(), keyCodec, valueCodec, 1, 10)
            cache1.set("key1", "persistent value")

            // when
            // Create a new cache instance pointing to the same directory
            val cache2 = PersistentKVCache(Path.of(tempDir.path), someProvider(), keyCodec, valueCodec, 1, 10)
            val value = cache2.get("key1")

            // then
            value shouldBe "persistent value"
        }
    }

    feature("eviction") {
        scenario("should evict entries that are too old") {
            // given
            val cache = PersistentKVCache(Path.of(tempDir.path), someProvider(), keyCodec, valueCodec, 1, 1)
            cache.set("key1", "value1")

            // then
            cache.get("key1") shouldBe "value1"

            // and when
            Thread.sleep(2000) // Wait for the entry to become stale
            val value = cache.get("key1")

            // then
            value.shouldBeNull()
        }
    }

    feature("getOrLoad") {
        scenario("should return cached value if present") {
            // given
            cache.set("key1", "cached value")

            // when
            val value = cache.getOrLoad("key1")

            // then
            value shouldBe "cached value"
        }

        scenario("should load and cache value if not present") {
            // given
            cache

            // when
            val value = cache.getOrLoad("key1")

            // then
            value shouldBe "loaded value for key1"
            cache.get("key1") shouldBe "loaded value for key1"
        }
    }
}) {
    init {
        testExecutionMode = TestExecutionMode.Concurrent
    }
}
