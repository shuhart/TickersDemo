package com.test.tickersdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.tickersdemo.core.Logger
import com.test.tickersdemo.network.TickersRepository
import com.test.tickersdemo.network.TradernetQuoteStream
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TickersViewModel(
    private val repository: TickersRepository,
    private val quoteStream: TradernetQuoteStream,
    private val logger: Logger,
) : ViewModel() {
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
            val tickers = try {
                repository.fetchTopSecurities()
            } catch (e: IOException) {
                logger.warn("Could not load top tickers", e)
                mutableState.value = TickersState.Error
                return@launch
            }
            mutableState.value = TickersState.Loaded(
                tickers.map { TickerUiModel(symbol = it.symbol, name = it.name) }
            )
            logger.debug("Loaded ${tickers.size} top tickers")

            val subscription = tickers.map { it.symbol }.distinct()
            if (subscription.isEmpty()) return@launch

            mutableState.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                .collectLatest { active ->
                    if (!active) return@collectLatest
                    quoteStream.observe(subscription).collect { quote ->
                        val symbol = quote.symbol ?: return@collect
                        val revision = quote.rev ?: return@collect

                        mutableState.update { current ->
                            val loaded = current as? TickersState.Loaded ?: return@update current
                            val ticker = loaded.tickers.firstOrNull { it.symbol == symbol } ?: return@update current
                            if (ticker.rev == revision) {
                                logger.debug("Ignored $symbol ticker update as the same revision has already been processed")
                                return@update current
                            }

                            loaded.copy(tickers = loaded.tickers.map {
                                if (it.symbol == symbol) it.update(quote) else it
                            })
                        }
                    }
                }
        }
    }
}

internal sealed interface TickersState {
    data object Loading : TickersState
    data object Error : TickersState
    data class Loaded(val tickers: List<TickerUiModel>) : TickersState
}
