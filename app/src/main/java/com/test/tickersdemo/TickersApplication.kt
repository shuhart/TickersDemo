package com.test.tickersdemo

import android.app.Application

class TickersApplication : Application() {
    internal val tickersComponent: TickersComponent by lazy { TickersComponent.createGraph() }
}
