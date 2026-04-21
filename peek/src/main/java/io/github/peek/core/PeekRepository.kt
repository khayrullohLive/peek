package io.github.peek.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Non-blocking repository — interceptor never waits on disk I/O.
 */
internal class PeekRepository(private val dao: PeekLogDao) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun observeAll(): Flow<List<PeekLog>> = dao.observeAll()

    suspend fun getAll(): List<PeekLog> = dao.getAll()

    suspend fun findById(id: Long): PeekLog? = dao.findById(id)

    fun insertAsync(log: PeekLog, onInserted: (Long) -> Unit) {
        scope.launch {
            val id = dao.insert(log)
            onInserted(id)
        }
    }

    fun updateAsync(log: PeekLog) {
        scope.launch { dao.update(log) }
    }

    suspend fun clear() = dao.clear()
}
