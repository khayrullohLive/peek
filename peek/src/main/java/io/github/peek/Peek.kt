package io.github.peek

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.room.Room
import io.github.peek.core.PeekDatabase
import io.github.peek.core.PeekInterceptor
import io.github.peek.core.PeekRepository
import io.github.peek.share.PeekShareManager
import okhttp3.Interceptor

object Peek {

    private var repository: PeekRepository? = null
    private var interceptor: PeekInterceptor? = null
    private var appContext: Context? = null

    /**
     * Compose'da observe qilinadigan logging holati.
     * true  = requestlar yozilmoqda
     * false = pauza (requestlar o'tkazib yuboriladi)
     *
     * Default: true — init bo'lishi bilanoq yozish boshlanadi.
     */
    internal val isLogging = mutableStateOf(true)

    fun init(context: Context) {
        if (repository != null) return

        val db = Room.databaseBuilder(
            context.applicationContext,
            PeekDatabase::class.java,
            "peek-logs.db",
        ).build()

        val repo   = PeekRepository(db.logDao())
        repository  = repo
        interceptor = PeekInterceptor(repo)
        appContext  = context.applicationContext
    }

    fun interceptor(): Interceptor =
        interceptor ?: error("Peek.init(context) must be called first")

    /** Logni yoqish */
    fun start() { isLogging.value = true }

    /** Logni pauza qilish — requestlar o'tadi lekin yozilmaydi */
    fun pause() { isLogging.value = false }

    /** Toggle: yoqilgan bo'lsa o'chiradi, o'chirilgan bo'lsa yoqadi */
    fun toggle() { isLogging.value = !isLogging.value }

    suspend fun clear() { repository().clear() }

    suspend fun buildShareIntent(): Intent {
        val ctx  = appContext ?: error("Peek.init(context) must be called first")
        val logs = repository().getAll()
        return PeekShareManager.buildShareIntent(ctx, logs)
    }

    internal fun repository(): PeekRepository =
        repository ?: error("Peek.init(context) must be called first")
}
