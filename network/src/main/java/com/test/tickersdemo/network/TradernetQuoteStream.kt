package com.test.tickersdemo.network

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.test.tickersdemo.core.Logger
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
import kotlin.time.Duration.Companion.milliseconds

class TradernetQuoteStream(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val logger: Logger,
) {
    fun observe(symbols: List<String>): Flow<TickerQuote> =
        callbackFlow {
            val subscription = gson.toJson(listOf("quotes", symbols))
            val request = Request.Builder().url("wss://wss.tradernet.com/").build()
            val subscriptionLock = Any()
            var subscribed = false
            var stopping = false
            val socket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    val enqueued = synchronized(subscriptionLock) {
                        if (stopping) return
                        webSocket.send(subscription).also { subscribed = it }
                    }
                    if (!enqueued) {
                        close(IOException("Could not enqueue Tradernet quote subscription"))
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val message = try {
                        JsonParser.parseString(text)
                    } catch (e: JsonParseException) {
                        logger.warn("Tradernet WebSocket message (invalid JSON)\n$text", e)
                        return
                    }
//                    logger.debugJson("Tradernet WebSocket message", message)
                    val quote = parseQuote(message) ?: return
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
                        close(IOException("Tradernet WebSocket closed with code $code: $reason"))
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    val message = response?.let { "Tradernet WebSocket failed (HTTP ${it.code})" }
                        ?: "Tradernet WebSocket failed"
                    if (!close(IOException(message, t))) {
                        logger.warn(message, t)
                    }
                }
            })

            awaitClose {
                synchronized(subscriptionLock) {
                    stopping = true
                    if (subscribed) {
                        socket.send("""["quotes",[]]""")
                    }
                    socket.close(1000, null)
                }
            }
        }.retryWhen { cause, _ ->
            if (cause !is IOException) {
                logger.warn("Tradernet quote stream stopped", cause)
                return@retryWhen false
            }
            logger.warn("Tradernet quote stream failed, reconnecting in 3 seconds", cause)
            delay(3_000.milliseconds)
            true
        }

    private fun parseQuote(json: JsonElement): TickerQuote? {
        val message = json as? JsonArray ?: return null
        if (message.size() < 2 || message[0] != JsonPrimitive("q") || !message[1].isJsonObject) {
            return null
        }
        return try {
            gson.fromJson(message[1], TickerQuote::class.java)
        } catch (e: JsonParseException) {
            logger.warn("Could not decode Tradernet quote", e)
            null
        }
    }
}
