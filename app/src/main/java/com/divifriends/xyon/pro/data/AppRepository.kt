package com.divifriends.xyon.pro.data

import com.divifriends.xyon.pro.data.model.AppData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * Single source of truth. Holds [AppData] in memory as a [StateFlow] and mirrors
 * every change to disk asynchronously.
 *
 * Writes are serialized on a dedicated single-thread executor so two rapid
 * mutations can never race on the backing file.
 */
class AppRepository(
    private val store: AppDataStore,
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "divi-persist")
        }.asCoroutineDispatcher()
    )
) {

    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data.asStateFlow()

    fun load() {
        _data.value = store.read()
    }

    fun mutate(transform: (AppData) -> AppData) {
        val next = transform(_data.value)
        _data.value = next
        persist(next)
    }

    fun replace(data: AppData) {
        _data.value = data
        persist(data)
    }

    fun clear() {
        replace(AppData())
    }

    private fun persist(data: AppData) {
        scope.launch { store.write(data) }
    }
}
