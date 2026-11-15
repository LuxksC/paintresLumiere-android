package com.luxkasTech.PaintresLumiere.feature.orders.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.format.formattedDate
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.data.order.OrderDetail
import com.luxkasTech.PaintresLumiere.data.order.OrderItem
import com.luxkasTech.PaintresLumiere.feature.orders.OrderStatusBadge

@Composable
fun OrderDetailRoute(onBack: () -> Unit, viewModel: OrderDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OrderDetailScreen(state, onBack, viewModel::refresh, viewModel::retry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(state: OrderDetailUiState, onBack: () -> Unit, onRefresh: () -> Unit, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding(),
    ) {
        Row(Modifier.padding(horizontal = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = PLColor.textPrimary)
            }
            Text(
                state.order?.let { stringResource(R.string.orders_number, it.displayNumber) } ?: stringResource(R.string.orders_title),
                style = PLFont.h2,
                color = PLColor.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
        }
        val order = state.order
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PLColor.goldMid)
            }
            order == null -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(PLSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.error?.asString().orEmpty(), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
                PLButton(stringResource(R.string.common_try_again), onClick = onRetry, modifier = Modifier.widthIn(max = 220.dp))
            }
            else -> PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(PLSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.xl),
                ) {
                    Header(order)
                    Section(stringResource(R.string.order_section_tracking)) { OrderTimelineView(state.timeline) }
                    Section(stringResource(R.string.order_section_items)) {
                        order.items.forEachIndexed { i, item ->
                            if (i > 0) HorizontalDivider(color = PLColor.borderSubtle)
                            ItemRow(item)
                        }
                    }
                    Section(stringResource(R.string.order_section_summary)) { Totals(order) }
                    order.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        Section(stringResource(R.string.order_section_notes)) {
                            Text(notes, style = PLFont.body, color = PLColor.textSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(order: OrderDetail) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.order_placed_on, order.createdAt.formattedDate()),
            style = PLFont.body,
            color = PLColor.textMuted,
            modifier = Modifier.weight(1f),
        )
        OrderStatusBadge(order.status)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        Text(title.uppercase(), style = PLFont.label.copy(letterSpacing = 1.5.sp), color = PLColor.textMuted, modifier = Modifier.semantics { heading() })
        Column(
            Modifier
                .fillMaxWidth()
                .background(PLColor.backgroundElevated, shape)
                .border(1.dp, PLColor.borderSubtle, shape)
                .padding(PLSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
        ) { content() }
    }
}

/** Frozen snapshot from checkout — never the current catalog data. */
@Composable
private fun ItemRow(item: OrderItem) {
    Row(Modifier.padding(vertical = PLSpacing.xs)) {
        Column(Modifier.weight(1f)) {
            Text(item.productName, style = PLFont.button, color = PLColor.textPrimary)
            Text(
                listOfNotNull(item.productSku, item.productColor, item.productDimensions).joinToString(" · "),
                style = PLFont.caption,
                color = PLColor.textMuted,
            )
            Text(
                stringResource(R.string.order_item_quantity, item.quantity, item.unitPrice.brlFormatted()),
                style = PLFont.caption,
                color = PLColor.textMuted,
            )
        }
        Text(item.lineTotal.brlFormatted(), style = PLFont.button, color = PLColor.textPrimary)
    }
}

@Composable
private fun Totals(order: OrderDetail) {
    TotalRow(stringResource(R.string.cart_subtotal), order.subtotalAmount.brlFormatted(), PLColor.textSecondary)
    if (order.discountAmount.signum() > 0) {
        TotalRow(stringResource(R.string.cart_discount), "−" + order.discountAmount.brlFormatted(), PLColor.success)
    }
    if (order.shippingAmount.signum() > 0) {
        TotalRow(stringResource(R.string.order_shipping), order.shippingAmount.brlFormatted(), PLColor.textSecondary)
    }
    HorizontalDivider(color = PLColor.borderSubtle)
    Row {
        Text(stringResource(R.string.cart_total), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.weight(1f))
        Text(order.totalAmount.brlFormatted(), style = PLFont.h2, color = PLColor.goldBright)
    }
}

@Composable
private fun TotalRow(label: String, value: String, color: Color) {
    Row {
        Text(label, style = PLFont.body, color = PLColor.textMuted, modifier = Modifier.weight(1f))
        Text(value, style = PLFont.body, color = color)
    }
}
