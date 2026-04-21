package io.github.peek

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import okhttp3.Interceptor
import okhttp3.Response

/**
 * No-op release variant of Peek.
 * All operations are replaced with empty implementations.
 */
object Peek {
    fun init(context: Context) = Unit
    fun interceptor(): Interceptor = NoOpInterceptor
    suspend fun clear() = Unit
    suspend fun buildShareIntent(): Intent = Intent()
}

@Composable
fun PeekBubble() = Unit

private object NoOpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request())
}
