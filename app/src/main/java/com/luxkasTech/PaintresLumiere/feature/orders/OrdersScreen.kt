package com.luxkasTech.PaintresLumiere.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLRemoteImage
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.format.formattedDate
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.data.order.OrderSummary
import java.math.BigDecimal
import java.time.Instant

@Composable
fun OrdersRoute(
    onOrderSelected: (orderId: String) -> Unit,
    onBrowseCatalog: () -> Unit,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OrdersScreen(state, onOrderSelected, onBrowseCatalog, viewModel::refresh, viewModel::retry, viewModel::loadMore)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    state: OrdersUiState,
    onOrderSelected: (String) -> Unit,
    onBrowseCatalog: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding(),
    ) {
        Column(Modifier.padding(start = PLSpacing.xl, end = PLSpacing.xl, top = PLSpacing.sm)) {
            Text(stringResource(R.string.orders_title), style = PLFont.h1, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.orders_subtitle), style = PLFont.label.copy(letterSpacing = 1.5.sp), color = PLColor.textMuted)
            PLDivider(Modifier.padding(top = PLSpacing.md))
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PLColor.goldMid)
            }
            state.error != null && state.orders.isEmpty() -> CenteredMessage(
                title = stringResource(R.string.orders_error_title),
                message = state.error.asString(),
                action = stringResource(R.string.common_try_again),
                onAction = onRetry,
            )
            state.orders.isEmpty() -> CenteredMessage(
                title = stringResource(R.string.orders_empty_title),
                message = stringResource(R.string.orders_empty_message),
                action = stringResource(R.string.cart_browse),
                onAction = onBrowseCatalog,
            )
            else -> PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(PLSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.orders, key = { it.id }) { order ->
                        OrderCard(order) { onOrderSelected(order.id) }
                    }
                    if (state.nextCursor != null) {
                        item(key = "loadMore") {
                            LaunchedEffect(state.orders.size) { onLoadMore() }
                            Box(Modifier.fillMaxWidth().padding(PLSpacing.md), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PLColor.goldMid, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderSummary, onClick: () -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(1.dp, PLColor.borderSubtle, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(PLSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(PLRadius.card - 4.dp))
                .background(PLColor.backgroundHighlight),
            contentAlignment = Alignment.Center,
        ) {
            if (order.thumbnailUrl != null) {
                PLRemoteImage(order.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, tint = PLColor.goldMid)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.orders_number, order.displayNumber), style = PLFont.button, color = PLColor.textPrimary)
            Text(
                "${order.createdAt.formattedDate()} · ${pluralStringResource(R.plurals.orders_item_count, order.itemCount, order.itemCount)}",
                style = PLFont.caption,
                color = PLColor.textMuted,
            )
            Text(order.totalAmount.brlFormatted(), style = PLFont.button, color = PLColor.goldBright)
        }
        OrderStatusBadge(order.status)
    }
}

@Composable
private fun CenteredMessage(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(PLSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, tint = PLColor.goldAntique, modifier = Modifier.size(40.dp))
        Text(title, style = PLFont.h2, color = PLColor.textPrimary, textAlign = TextAlign.Center)
        Text(message, style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
        PLButton(action, onClick = onAction, modifier = Modifier.widthIn(max = 240.dp))
    }
}

@Preview
@Composable
private fun OrdersPreview() {
    val orders = listOf(
        OrderSummary("1", "PL-1001", OrderStatus.IN_PRODUCTION, BigDecimal("348.70"), Instant.parse("2026-10-08T13:20:00Z"), 14),
        OrderSummary("2", "PL-0987", OrderStatus.DELIVERED, BigDecimal("239.40"), Instant.parse("2026-09-21T18:02:00Z"), 6),
    )
    PaintresLumiereTheme { OrdersScreen(OrdersUiState(orders, isLoading = false), {}, {}, {}, {}, {}) }
}
