package com.test.tickersdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.test.tickersdemo.ui.TickersScreen
import com.test.tickersdemo.ui.TickersViewModel
import com.test.tickersdemo.ui.theme.TickersDemoTheme

class MainActivity : ComponentActivity() {
    private val tickersViewModel: TickersViewModel by viewModels {
        val component = (application as TickersApplication).tickersComponent
        viewModelFactory { initializer { component.tickersViewModel() } }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TickersDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TickersScreen(tickersViewModel, Modifier.padding(innerPadding))
                }
            }
        }
    }
}
