package com.test.tickersdemo.core

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement

class Logger(gson: Gson) {
    private val prettyGson = gson.newBuilder().setPrettyPrinting().serializeNulls().create()

    fun debug(message: String) {
        Log.d(TAG, message)
    }

    fun debugJson(message: String, json: JsonElement) {
        debug("$message\n${prettyGson.toJson(json)}")
    }

    fun warn(message: String, cause: Throwable) {
        Log.w(TAG, message, cause)
    }

    fun warn(message: String) {
        Log.w(TAG, message)
    }

    private companion object {
        const val TAG = "TickersDemo"
    }
}
