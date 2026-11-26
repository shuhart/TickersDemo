package com.test.tickersdemo.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.test.tickersdemo.data.Ticker

@Composable
fun TickersScreen(viewModel: TickersViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.tickers_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val current = state) {
                TickersState.Loading -> CircularProgressIndicator()
                TickersState.Error -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.tickers_load_error))
                    Button(onClick = viewModel::retry) {
                        Text(stringResource(R.string.tickers_retry))
                    }
                }
                is TickersState.Loaded -> if (current.tickers.isEmpty()) {
                    Text(stringResource(R.string.tickers_empty))
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(current.tickers) { ticker ->
                            TickerRow(ticker)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TickerRow(ticker: Ticker) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = "https://tradernet.com/logos/get-logo-by-ticker?ticker=" +
                Uri.encode(ticker.symbol.lowercase()),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Column {
            Text(ticker.symbol, style = MaterialTheme.typography.titleMedium)
            ticker.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
