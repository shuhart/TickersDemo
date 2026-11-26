package com.test.tickersdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.tickersdemo.data.Ticker
import com.test.tickersdemo.network.TickersRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TickersViewModel(private val repository: TickersRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<TickersState>(TickersState.Loading)
    internal val state = mutableState.asStateFlow()

    init {
        load()
    }

    internal fun retry() {
        if (mutableState.compareAndSet(TickersState.Error, TickersState.Loading)) {
            load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            mutableState.value = try {
                TickersState.Loaded(repository.fetchTopSecurities())
            } catch (_: IOException) {
                TickersState.Error
            }
        }
    }
}

internal sealed interface TickersState {
    data object Loading : TickersState
    data object Error : TickersState
    data class Loaded(val tickers: List<Ticker>) : TickersState
}
