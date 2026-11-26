package com.test.tickersdemo.ui

import com.test.tickersdemo.data.TickerQuote

internal enum class PercentStyle { GreenBackground, RedBackground, Green, Red }

internal data class TickerUiModel(
    val symbol: String,
    val name: String?,
    val percentChange: Double? = null,
    val exchange: String? = null,
    val lastPrice: Double? = null,
    val priceChange: Double? = null,
    val percentStyle: PercentStyle? = null,
    val rev: String? = null,
) {
    fun update(quote: TickerQuote): TickerUiModel {
        val percent = quote.percentChange ?: percentChange
        val newPrice = quote.lastPrice
        val plainStyle = percent?.let { if (it > 0) PercentStyle.Green else PercentStyle.Red }
        val style = when {
            percentStyle == null -> plainStyle
            lastPrice != null && newPrice != null -> when {
                lastPrice < newPrice -> PercentStyle.GreenBackground
                else -> PercentStyle.RedBackground
            }
            else -> percentStyle
        }
        return copy(
            percentChange = percent,
            exchange = quote.exchange ?: exchange,
            lastPrice = newPrice ?: lastPrice,
            priceChange = quote.priceChange ?: priceChange,
            percentStyle = style,
            rev = quote.rev,
        )
    }
}
