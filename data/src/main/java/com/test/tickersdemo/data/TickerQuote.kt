package com.test.tickersdemo.data

import com.google.gson.annotations.SerializedName

data class TickerQuote(
    @SerializedName("c") val symbol: String?,
    @SerializedName("pcp") val percentChangeSinceLastClosing: Double?,
    @SerializedName("ltr") val stockExchange: String?,
    @SerializedName("ltp") val lastDealPrice: Double?,
    @SerializedName("chg") val priceChangeSinceLastClosing: Double?,
    @SerializedName("rev") val rev: String? = null,
)
