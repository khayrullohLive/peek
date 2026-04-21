package io.github.peek.core

import io.github.peek.Peek
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.io.IOException

internal class PeekInterceptor(
    private val repository: PeekRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Pauza holatida — request o'tadi lekin hech narsa yozilmaydi
        if (!Peek.isLogging.value) {
            return chain.proceed(chain.request())
        }

        val request   = chain.request()
        val startTime = System.currentTimeMillis()

        var logId = -1L
        val pending = request.toPeekLog(startTime)
        repository.insertAsync(pending) { id -> logId = id }

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            val errorLog = pending.copy(
                id         = logId.takeIf { it > 0 } ?: 0,
                durationMs = System.currentTimeMillis() - startTime,
                error      = e.message ?: e::class.simpleName,
            )
            if (errorLog.id > 0) repository.updateAsync(errorLog)
            throw e
        }

        val endTime      = System.currentTimeMillis()
        val responseBody = try {
            response.peekBody(MAX_BODY_BYTES).string()
        } catch (_: Exception) {
            null
        }

        val updatedLog = pending.copy(
            id              = logId.takeIf { it > 0 } ?: 0,
            durationMs      = endTime - startTime,
            statusCode      = response.code,
            responseHeaders = response.headers.joinToString("\n") { "${it.first}: ${it.second}" },
            responseBody    = responseBody,
        )
        if (updatedLog.id > 0) repository.updateAsync(updatedLog)

        return response
    }

    private fun Request.toPeekLog(startTime: Long): PeekLog {
        val bodyString = body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8().take(MAX_BODY_BYTES.toInt())
            } catch (_: Exception) {
                null
            }
        }

        return PeekLog(
            endpoint       = url.toString(),
            date           = startTime,
            method         = method,
            host           = url.host,
            path           = url.encodedPath,
            requestHeaders = headers.joinToString("\n") { "${it.first}: ${it.second}" },
            requestBody    = bodyString,
        )
    }

    companion object {
        private const val MAX_BODY_BYTES = 250_000L
    }
}
