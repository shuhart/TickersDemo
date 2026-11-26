package com.test.tickersdemo.ui

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

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
private fun TickerRow(ticker: TickerUiModel) {
    val placeholder = stringResource(R.string.quote_placeholder)
    val exchange = ticker.exchange?.takeIf { it.isNotBlank() } ?: placeholder
    val price = ticker.lastPrice?.let { stringResource(R.string.quote_number, it) } ?: placeholder
    val change = ticker.priceChange?.let { stringResource(R.string.quote_number, it) } ?: placeholder

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
                QuotePercent(ticker)
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
private fun QuotePercent(ticker: TickerUiModel) {
    val flash = ticker.priceFlash
    var previousFlash by remember(ticker.symbol) { mutableStateOf(flash) }
    val flashOpacity = remember(ticker.symbol, flash) { Animatable(0f) }
    LaunchedEffect(flashOpacity) {
        if (flash != null && flash != previousFlash) {
            previousFlash = flash
            flashOpacity.snapTo(1f)
            delay(1000.milliseconds)
            flashOpacity.animateTo(0f, tween(durationMillis = 1000, easing = LinearOutSlowInEasing))
        }
    }

    val percent = ticker.percentChange
    val text = percent?.let { stringResource(R.string.quote_percent, it) }
        ?: stringResource(R.string.quote_placeholder)
    val darkSurface = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val green = Color(0xFF167044)
    val red = Color(0xFFB3261E)
    val isUp = flash?.increased ?: percent?.let { it > 0 }
    val restingColor = when (isUp) {
        true -> if (darkSurface) Color(0xFF73D9A0) else green
        false -> if (darkSurface) Color(0xFFFFB4AB) else red
        null -> LocalContentColor.current
    }
    val opacity = flashOpacity.value
    val color = lerp(restingColor, Color.White, opacity)
    val background = when (flash?.increased) {
        true -> green.copy(alpha = opacity)
        false -> red.copy(alpha = opacity)
        null -> Color.Transparent
    }
    Text(
        text = text,
        modifier = Modifier.background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp),
        color = color,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    )
}
