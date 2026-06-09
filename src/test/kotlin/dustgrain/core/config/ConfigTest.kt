import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import one.cheily.dustgrain.core.config.AppConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe


class ConfigTest : FeatureSpec({
    val RESOURCE_PATH = "/dustgrain/core/config/"
    val logger = KotlinLogging.logger {}

    feature("Config Loader") {
        scenario("should load sample values") {
            shouldNotThrow<ConfigException> {
                ConfigLoaderBuilder.default()
                    .addResourceSource(RESOURCE_PATH + "application-sample.yml")
                    .addDecoder(AppConfig.TableFieldFormatDecoder())
                    .build()
                    .loadConfigOrThrow<AppConfig>()
            }
        }

        scenario("should fail with missing or invalid client url") {
            shouldThrow<ConfigException> {
                ConfigLoaderBuilder.default()
                    .addResourceSource(RESOURCE_PATH + "application-invalid-url.yml")
                    .addDecoder(AppConfig.TableFieldFormatDecoder())
                    .build()
                    .loadConfigOrThrow<AppConfig>()
            }
        }

        scenario("should fail with invalid config structure") {
            shouldThrow<ConfigException> {
                ConfigLoaderBuilder.default()
                    .addResourceSource(RESOURCE_PATH + "application-invalid-format.yml")
                    .addDecoder(AppConfig.TableFieldFormatDecoder())
                    .build()
                    .loadConfigOrThrow<AppConfig>()
            }
        }
    }
})
