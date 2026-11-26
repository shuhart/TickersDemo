package com.test.tickersdemo.network

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.test.tickersdemo.data.TickerQuote
import java.io.IOException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retryWhen
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class TradernetQuoteStream(
    private val client: OkHttpClient,
    private val gson: Gson,
) {
    fun observe(symbols: List<String>): Flow<TickerQuote> =
        callbackFlow {
            val subscription = gson.toJson(listOf("quotes", symbols))
            val request = Request.Builder().url("wss://wss.tradernet.com/").build()
            val socket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    val quote = parseQuote(text) ?: return
                    val delivery = trySend(quote)
                    if (delivery.isFailure && !delivery.isClosed) {
                        close(IOException("Tradernet quote buffer is full"))
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    webSocket.close(1000, null)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    if (code == 1000) {
                        close()
                    } else {
                        close(IOException("Tradernet WebSocket closed with code $code"))
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    close(IOException("Tradernet WebSocket failed", t))
                }
            })

            socket.send(subscription)
            awaitClose {
                socket.send("""["quotes",[]]""")
                socket.close(1000, null)
            }
        }.retryWhen { cause, _ ->
            if (cause !is IOException) return@retryWhen false
            delay(3_000)
            true
        }

    private fun parseQuote(text: String): TickerQuote? {
        return try {
            val message = JsonParser.parseString(text) as? JsonArray ?: return null
            if (message[0] != JsonPrimitive("q") || !message[1].isJsonObject) {
                return null
            }
            gson.fromJson(message[1], TickerQuote::class.java)
        } catch (_: JsonParseException) {
            null
        }
    }
}
