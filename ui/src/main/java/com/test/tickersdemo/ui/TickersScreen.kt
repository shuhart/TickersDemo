package com.test.tickersdemo.ui

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
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
                            TickerRow(ticker, current.quotes[ticker.symbol])
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TickerRow(ticker: Ticker, quote: DisplayQuote?) {
    val placeholder = stringResource(R.string.quote_placeholder)
    val exchange = quote?.exchange?.takeIf { it.isNotBlank() } ?: placeholder
    val price = quote?.lastPrice?.let { stringResource(R.string.quote_number, it) } ?: placeholder
    val change = quote?.priceChange?.let { stringResource(R.string.quote_number, it) } ?: placeholder

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TickerLogo(ticker.symbol)
                Text(
                    text = ticker.symbol,
                    modifier = Modifier.weight(1f),
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                QuotePercent(quote)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.quote_exchange_and_name, exchange, ticker.name ?: placeholder),
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(stringResource(R.string.quote_price_and_change, price, change))
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
        )
    }
}

@Composable
private fun TickerLogo(symbol: String) {
    val context = LocalContext.current
    val size = 32.dp
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val request = remember(context, symbol, sizePx) {
        ImageRequest.Builder(context)
            .data("https://tradernet.com/logos/get-logo-by-ticker?ticker=" + Uri.encode(symbol.lowercase()))
            .size(sizePx)
            .build()
    }
    val painter = rememberAsyncImagePainter(request)
    if (painter.state is AsyncImagePainter.State.Success) {
        Image(painter, contentDescription = null, modifier = Modifier.size(size))
    }
}

@Composable
private fun QuotePercent(quote: DisplayQuote?) {
    val text = quote?.percentChange?.let { stringResource(R.string.quote_percent, it) }
        ?: stringResource(R.string.quote_placeholder)
    val darkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val green = Color(0xFF167044)
    val red = Color(0xFFB3261E)
    val background = when (quote?.percentStyle) {
        PercentStyle.GreenBackground -> green
        PercentStyle.RedBackground -> red
        else -> Color.Unspecified
    }
    val color = when (quote?.percentStyle) {
        PercentStyle.GreenBackground, PercentStyle.RedBackground -> Color.White
        PercentStyle.Green -> if (darkSurface) Color(0xFF73D9A0) else green
        PercentStyle.Red -> if (darkSurface) Color(0xFFFFB4AB) else red
        null -> Color.Unspecified
    }
    val modifier = if (background == Color.Unspecified) {
        Modifier
    } else {
        Modifier.background(background, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp)
    }
    Text(text = text, modifier = modifier, color = color, fontSize = 28.sp, lineHeight = 34.sp)
}
