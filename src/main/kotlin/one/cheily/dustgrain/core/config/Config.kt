package one.cheily.dustgrain.core.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.Validated
import one.cheily.dustgrain.core.cache.CacheMode
import java.net.URL
import kotlin.reflect.KType

private const val CONFIG_PATH = "/application.yml"

@JvmOverloads
fun loadConfig(configPath: String = CONFIG_PATH) = ConfigLoaderBuilder.default()
    .addResourceSource(configPath)
    .addDecoder(AppConfig.TableFieldFormatDecoder())
    .build()
    .loadConfigOrThrow<AppConfig>()

data class AppConfig(
    val appInfo: AppInfo,
    val cache: Cache,
    val client: Client,
    val cargoQueries: List<GameWiki>?
) {
    //===================app info===================
    data class AppInfo(
        val name: String,
        val version: String,
        val author: String
    )

    //====================cache====================
    data class Cache(
        val headers: CacheConf,
    )

    data class CacheConf(
        val version: Int,
        val maxAgeSeconds: Long,
        val mode: CacheMode
    )


    //====================client====================
    data class Client(
        val url: URL,
        val timeout: Long,
        val userAgent: String
    )


    //====================wikis====================
    @JvmInline
    value class GameWiki(val config: Map<GameName, List<GameTable>>) {
        @JvmInline
        value class GameName(val name: String)
    }

    @JvmInline
    value class GameTable(val config: Map<TableName, List<TableFieldFormat>>) {
        @JvmInline
        value class TableName(val name: String)
    }

    data class TableFieldFormat(
        val key: Column,
        val value: FormatMethodReference?
    ) {
        @JvmInline
        value class Column(val name: String)
        @JvmInline
        value class FormatMethodReference(val name: String)
    }

    class TableFieldFormatDecoder : Decoder<TableFieldFormat> {
        override fun decode(
            node: Node,
            type: KType,
            context: DecoderContext
        ): ConfigResult<TableFieldFormat> {
            return when (node) {
                is StringNode -> Validated.Valid(TableFieldFormat(TableFieldFormat.Column(node.value), null))
                is MapNode -> {
                    val entry = node.map.entries.firstOrNull()
                        ?: return Validated.Invalid(ConfigFailure.Generic("Map cannot be empty for Line"))

                    if (node.map.size > 1) {
                        return Validated.Invalid(ConfigFailure.Generic("Map must have exactly one entry for Line"))
                    }

                    val fmr = when (val valueNode = entry.value) {
                        is StringNode -> TableFieldFormat.FormatMethodReference(valueNode.value)
                        is NullNode -> null
                        else -> return Validated.Invalid(ConfigFailure.Generic("Value must be a string or null"))
                    }
                    Validated.Valid(TableFieldFormat(TableFieldFormat.Column(entry.key), fmr))
                }

                else -> Validated.Invalid(ConfigFailure.Generic("Node must be a string or a map"))
            }

        }

        override fun supports(type: KType): Boolean {
            return type.classifier == TableFieldFormat::class
        }
    }
}
