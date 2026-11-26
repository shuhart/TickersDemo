package com.test.tickersdemo.data

import com.google.gson.annotations.SerializedName

data class TickerQuote(
    @SerializedName("c") val symbol: String?,
    @SerializedName("pcp") val percentChange: Double?,
    @SerializedName("ltr") val exchange: String?,
    @SerializedName("ltp") val lastPrice: Double?,
    @SerializedName("chg") val priceChange: Double?
)
