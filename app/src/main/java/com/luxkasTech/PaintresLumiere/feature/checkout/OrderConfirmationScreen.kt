package com.luxkasTech.PaintresLumiere.feature.checkout

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.feature.orders.OrderStatusBadge
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus

/**
 * Shown after `POST /orders` succeeds. Checkout is already removed from the back stack, so Back
 * (system or button) returns to the catalog — never to a screen that could resubmit the order.
 */
@Composable
fun OrderConfirmationScreen(
    orderNumber: String,
    total: String,
    onViewOrder: () -> Unit,
    onBackToCatalog: () -> Unit,
) {
    BackHandler(onBack = onBackToCatalog)
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .systemBarsPadding()
            .padding(PLSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PLColor.success, modifier = Modifier.size(64.dp))
        Text(
            stringResource(R.string.confirmation_title),
            style = PLFont.h1,
            color = PLColor.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(R.string.orders_number, orderNumber), style = PLFont.h2, color = PLColor.goldBright)
        Text(total, style = PLFont.body, color = PLColor.textSecondary)
        OrderStatusBadge(OrderStatus.RECEIVED)
        Text(stringResource(R.string.confirmation_next_steps), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
        PLButton(stringResource(R.string.confirmation_view_order), onClick = onViewOrder, modifier = Modifier.padding(top = PLSpacing.md))
        PLButton(stringResource(R.string.confirmation_back_to_catalog), onClick = onBackToCatalog, type = PLButtonType.Ghost)
    }
}
