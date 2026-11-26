package com.test.tickersdemo.data

import com.google.gson.annotations.SerializedName

data class TopSecuritiesRequest(
    @SerializedName("exchange") val exchange: String,
    @SerializedName("type") val type: String,
    @SerializedName("gainers") val gainers: Boolean,
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("days") val days: Int,
)

data class TopSecurities(
    @SerializedName("tickers") val tickers: List<String>?,
    @SerializedName("details") val details: Map<String, SecurityDetails>?,
)

data class SecurityDetails(
    @SerializedName("name") val name: String?,
)

data class Ticker(
    val symbol: String,
    val name: String?,
)
