package com.test.tickersdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.tickersdemo.data.Ticker
import com.test.tickersdemo.data.TickerQuote
import com.test.tickersdemo.core.Logger
import com.test.tickersdemo.network.TickersRepository
import com.test.tickersdemo.network.TradernetQuoteStream
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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
            var loaded = TickersState.Loaded(tickers)
            mutableState.value = loaded
            logger.debug("Loaded ${tickers.size} top tickers")

            val symbols = tickers.map { it.symbol }.toSet()
            if (symbols.isEmpty()) return@launch
            val subscription = symbols.toList()

            mutableState.subscriptionCount
                .map { it > 0 }
                .distinctUntilChanged()
                .collectLatest { active ->
                    if (!active) return@collectLatest
                    quoteStream.observe(subscription).collect { quote ->
                        val symbol = quote.symbol
                        if (symbol != null && symbol in symbols) {
                            val display = loaded.quotes[symbol] ?: DisplayQuote()
                            loaded = loaded.copy(quotes = loaded.quotes + (symbol to display.update(quote)))
                            mutableState.value = loaded
                        }
                    }
                }
        }
    }
}

internal sealed interface TickersState {
    data object Loading : TickersState
    data object Error : TickersState
    data class Loaded(
        val tickers: List<Ticker>,
        val quotes: Map<String, DisplayQuote> = emptyMap(),
    ) : TickersState
}

internal enum class PercentStyle { GreenBackground, RedBackground, Green, Red }

internal data class DisplayQuote(
    val percentChange: Double? = null,
    val exchange: String? = null,
    val lastPrice: Double? = null,
    val priceChange: Double? = null,
    val percentStyle: PercentStyle? = null,
) {
    fun update(quote: TickerQuote): DisplayQuote {
        val percent = quote.percentChange ?: percentChange
        val plainStyle = percent?.let { if (it > 0) PercentStyle.Green else PercentStyle.Red }
        val style = when {
            percentStyle == null -> plainStyle
            else -> percentStyle
        }
        return copy(
            percentChange = percent,
            exchange = quote.exchange ?: exchange,
            lastPrice = quote.lastPrice ?: lastPrice,
            priceChange = quote.priceChange ?: priceChange,
            percentStyle = style,
        )
    }
}
