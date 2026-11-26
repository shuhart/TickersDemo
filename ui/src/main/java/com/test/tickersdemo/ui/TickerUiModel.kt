package com.test.tickersdemo.ui

import com.test.tickersdemo.data.TickerQuote

internal data class TickerUiModel(
    val symbol: String,
    val name: String?,
    val percentChange: Double? = null,
    val exchange: String? = null,
    val lastPrice: Double? = null,
    val priceChange: Double? = null,
    val rev: String? = null,
    val priceFlash: PriceFlash? = null,
) {
    fun update(quote: TickerQuote): TickerUiModel {
        val newPrice = quote.lastPrice
        val newFlash = if (lastPrice != null && newPrice != null && newPrice != lastPrice) {
            PriceFlash(increased = newPrice > lastPrice)
        } else {
            priceFlash
        }
        return copy(
            percentChange = quote.percentChange ?: percentChange,
            exchange = quote.exchange ?: exchange,
            lastPrice = newPrice ?: lastPrice,
            priceChange = quote.priceChange ?: priceChange,
            rev = quote.rev,
            priceFlash = newFlash,
        )
    }
}

internal class PriceFlash(val increased: Boolean)
