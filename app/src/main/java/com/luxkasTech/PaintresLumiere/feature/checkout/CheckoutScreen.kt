package com.luxkasTech.PaintresLumiere.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.data.order.OrderDetail

@Composable
fun CheckoutRoute(
    onBack: () -> Unit,
    onOrderPlaced: (OrderDetail) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutViewModel.Event.OrderPlaced -> onOrderPlaced(event.order)
            }
        }
    }
    CheckoutScreen(state, onBack, viewModel::onNotesChange, viewModel::placeOrder)
}

@Composable
fun CheckoutScreen(state: CheckoutUiState, onBack: () -> Unit, onNotesChange: (String) -> Unit, onPlaceOrder: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Row(Modifier.padding(horizontal = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = PLColor.textPrimary)
            }
            Text(stringResource(R.string.checkout_title), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(PLSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.lg),
        ) {
            val shape = RoundedCornerShape(PLRadius.card)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(PLColor.backgroundElevated, shape)
                    .border(1.dp, PLColor.borderSubtle, shape)
                    .padding(PLSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
            ) {
                Text(stringResource(R.string.order_section_items).uppercase(), style = PLFont.label.copy(letterSpacing = 1.5.sp), color = PLColor.textMuted)
                state.cart.items.forEach { item ->
                    Row {
                        Text(
                            "${item.quantity}× ${item.name} · ${stringResource(item.color.labelRes)} · ${item.sizeLabel}",
                            style = PLFont.body,
                            color = PLColor.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(item.lineTotal.brlFormatted(), style = PLFont.body, color = PLColor.textPrimary)
                    }
                }
                HorizontalDivider(color = PLColor.borderSubtle)
                SummaryRow(stringResource(R.string.cart_subtotal), state.cart.subtotal.brlFormatted())
                if (state.cart.discountTotal.signum() > 0) {
                    SummaryRow(stringResource(R.string.cart_discount), "−" + state.cart.discountTotal.brlFormatted())
                }
                Row {
                    Text(stringResource(R.string.cart_total), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.weight(1f))
                    Text(state.cart.total.brlFormatted(), style = PLFont.h2, color = PLColor.goldBright)
                }
                Text(stringResource(R.string.checkout_server_total_hint), style = PLFont.caption, color = PLColor.textDisabled)
            }
            PLInputField(
                label = stringResource(R.string.checkout_notes),
                value = state.notes,
                onValueChange = onNotesChange,
                placeholder = stringResource(R.string.checkout_notes_placeholder),
            )
            state.error?.let { error ->
                Column {
                    Text(error.asString(), style = PLFont.body, color = PLColor.error)
                    if (state.isStockConflict) {
                        TextButton(onClick = onBack) {
                            Text(stringResource(R.string.checkout_back_to_cart), style = PLFont.link, color = PLColor.goldMid)
                        }
                    }
                }
            }
        }
        Column(
            Modifier
                .background(PLColor.backgroundSecondary)
                .navigationBarsPadding()
                .padding(horizontal = PLSpacing.xl, vertical = PLSpacing.md),
        ) {
            PLButton(
                text = stringResource(R.string.checkout_place_order),
                onClick = onPlaceOrder,
                type = if (state.isSubmitting) PLButtonType.Loading else PLButtonType.Primary,
                enabled = state.canSubmit,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row {
        Text(label, style = PLFont.body, color = PLColor.textMuted, modifier = Modifier.weight(1f))
        Text(value, style = PLFont.body, color = PLColor.textSecondary)
    }
}
