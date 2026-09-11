package one.cheily.dustgrain.core

import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import one.cheily.dustgrain.core.fetching.DustloopErrorResponse

class DustloopErrorException(
    httpResponse: HttpResponse,
    error: DustloopErrorResponse,
) : ResponseException(
    httpResponse,
    "Dustloop API Error: $error"
)