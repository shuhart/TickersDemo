package com.test.tickersdemo.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal data class TickerUiModel(
    val symbol: String,
    val name: String?,
    val percentChange: Double? = null,
    val exchange: String? = null,
    val lastPrice: Double? = null,
    val priceChange: Double? = null,
    val rev: String? = null,
    val priceFlash: PriceFlash? = null,
)

internal class PriceFlash(val increased: Boolean)

internal class TickerRowState(initialTicker: TickerUiModel) {
    var ticker by mutableStateOf(initialTicker)
        private set

    fun update(ticker: TickerUiModel) {
        this.ticker = ticker
    }
}
