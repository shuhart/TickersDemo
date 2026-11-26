package com.test.tickersdemo.network

import com.google.gson.JsonParseException
import com.test.tickersdemo.data.Ticker
import com.test.tickersdemo.data.TopSecuritiesRequest
import java.io.IOException

class TickersRepository(private val api: TradernetApi) {
    suspend fun fetchTopSecurities(): List<Ticker> {
        val request = TopSecuritiesRequest(
            exchange = "usa",
            type = "stocks",
            gainers = true,
            limit = 30,
            offset = 0,
            days = 365,
        )

        // we can do better than to process errors separately in each repo, but it will do for test task
        val response = try {
            api.getTopSecurities(request)
        } catch (e: JsonParseException) {
            throw IOException("Invalid Tradernet response", e)
        }
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw IOException("Tradernet returned HTTP ${response.code()}")
        }

        val result = response.body()
        val symbols = result?.tickers ?: throw IOException("Tradernet response is missing tickers")
        return symbols.map { symbol ->
            Ticker(symbol, result.details?.get(symbol)?.name)
        }
    }
}
