package de.danotter.smooothweather.shared.ui

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.currentCompositeKeyHash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.reflect.KClass

class ViewModelStore {

    private val store = mutableMapOf<String, BaseViewModel>()

    fun <VM : BaseViewModel> store(key: String, viewModel: VM) {
        store[key] = viewModel
    }

    fun <VM : BaseViewModel> get(key: String): VM? {
        @Suppress("UNCHECKED_CAST")
        return store[key] as VM?
    }

    fun dispose() {
        store.values.forEach { it.close() }
        store.clear()
    }
}

@Composable
inline fun <VM : BaseViewModel> viewModel(clazz: KClass<VM>, provider: () -> VM): VM {
    val configStore = LocalViewModelStore.current
    val key = "${clazz.qualifiedName}:$currentCompositeKeyHash"

    return configStore.get(key) ?: provider().also {
        configStore.store(key, it)
    }
}

val LocalViewModelStore = compositionLocalOf<ViewModelStore> {
    error("Needs to be provided")
}

abstract class BaseViewModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val viewModelScope get() = scope

    @CallSuper
    open fun close() {
        scope.cancel()
    }
}