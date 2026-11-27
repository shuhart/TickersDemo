package com.test.tickersdemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.tickersdemo.core.Logger
import com.test.tickersdemo.data.TickerQuote
import com.test.tickersdemo.network.TickersRepository
import com.test.tickersdemo.network.TradernetQuoteStream
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
            mutableState.value = TickersState.Loaded(
                tickers.map { TickerUiModel(symbol = it.symbol, name = it.name) }
            )
            logger.debug("Loaded ${tickers.size} top tickers")

            val subscription = tickers.map { it.symbol }.distinct()
            if (subscription.isEmpty()) return@launch

            mutableState.subscriptionCount
                .map { it > 0 }
                .collectLatest { active ->
                    if (!active) return@collectLatest
                    quoteStream.observe(subscription).collect { quote ->
                        val symbol = quote.symbol ?: return@collect
                        val revision = quote.rev ?: return@collect

                        val loaded = mutableState.value as? TickersState.Loaded ?: return@collect
                        val row = loaded.rowFor(symbol) ?: return@collect
                        val ticker = row.ticker
                        if (ticker.rev == revision) {
                            logger.debug("Ignored $symbol ticker update as the same revision has already been processed")
                            return@collect
                        }

                        if (quote.lastDealPrice == null || quote.priceChangeSinceLastClosing == null ||
                            quote.percentChangeSinceLastClosing == null) {
                            logger.warn(
                                "Anomaly ticker: $symbol. Old price ${ticker.lastPrice}, new price (ltp) ${quote.lastDealPrice}, " +
                                        "old % ${ticker.percentChange}, new % (pcp) ${quote.percentChangeSinceLastClosing}, " +
                                        "new price change (chg) ${quote.priceChangeSinceLastClosing}"
                            )
                        }

                        // not sure what to do with this case, ignoring for now...
                        if (quote.lastDealPrice == null && quote.priceChangeSinceLastClosing == null &&
                            quote.percentChangeSinceLastClosing == null) {
                            logger.warn("Update for ticker $symbol without either prices present. Ignoring")
                            return@collect
                        }

                        updateRow(row, quote)
                    }
                }
        }
    }

    private fun updateRow(rowState: TickerRowState, quote: TickerQuote) {
        val newPrice = quote.lastDealPrice
        val oldTicker = rowState.ticker
        val newFlash = if (oldTicker.lastPrice != null && newPrice != null && newPrice != oldTicker.lastPrice) {
            PriceFlash(increased = newPrice > oldTicker.lastPrice)
        } else {
            null
        }
        val newTicker = oldTicker.copy(
            percentChange = quote.percentChangeSinceLastClosing ?: oldTicker.percentChange,
            exchange = quote.stockExchange ?: oldTicker.exchange,
            lastPrice = newPrice ?: oldTicker.lastPrice,
            priceChange = quote.priceChangeSinceLastClosing ?: oldTicker.priceChange,
            rev = quote.rev,
            priceFlash = newFlash,
        )
        rowState.update(newTicker)
    }
}

internal sealed interface TickersState {
    data object Loading : TickersState
    data object Error : TickersState
    class Loaded(tickers: List<TickerUiModel>) : TickersState {
        private val rowsBySymbol = mutableMapOf<String, TickerRowState>()
        val rows = tickers.map { ticker ->
            rowsBySymbol.getOrPut(ticker.symbol) { TickerRowState(ticker) }
        }

        fun rowFor(symbol: String): TickerRowState? = rowsBySymbol[symbol]
    }
}
