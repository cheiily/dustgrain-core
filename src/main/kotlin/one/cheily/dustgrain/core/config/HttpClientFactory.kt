package one.cheily.dustgrain.core.config

import one.cheily.dustgrain.core.Application
import one.cheily.dustgrain.core.DustloopErrorException
import one.cheily.dustgrain.core.fetching.DustloopErrorResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import one.cheily.dustgrain.core.DustloopApiException

fun getHttpClient(
    appProfile: AppProfile = Application.profile,
    config: AppConfig = Application.config
) = HttpClient(CIO) {
    expectSuccess = true

    install(UserAgent) {
        agent = "${config.appInfo.name} ($appProfile) ${config.client.userAgent}".trim()
    }
    install(HttpTimeout) {
        requestTimeoutMillis = config.client.timeout
    }
    install(HttpRequestRetry) {
        retryOnServerErrors(maxRetries = 3)
    }
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        })
    }
    HttpResponseValidator {
        validateResponse { response ->
            val error: DustloopErrorResponse = try {
                response.body()
            } catch (e: JsonConvertException) {
                return@validateResponse
            }
            throw DustloopErrorException(response, error)
        }
    }

    defaultRequest {
        url(config.client.url.toString() + "?format=json")
        headers.appendIfNameAbsent("Accept", "application/json")
        headers.appendIfNameAbsent("Accept-Charset", "utf-8")
        headers.appendIfNameAbsent(
            "Connection",
            when (appProfile) {
                AppProfile.CLI -> "close"
                AppProfile.LIB -> "keep-alive"
            }
        )
    }
}
