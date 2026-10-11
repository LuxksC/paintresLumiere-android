package com.luxkasTech.PaintresLumiere.feature.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod

@Composable
fun PaymentMethodRoute(
    onBack: () -> Unit,
    onChargeCreated: (orderId: String, payment: Payment) -> Unit,
    onCardRequested: (orderId: String, installments: Int) -> Unit,
    viewModel: PaymentMethodViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PaymentMethodViewModel.Event.ChargeCreated -> onChargeCreated(event.orderId, event.payment)
                is PaymentMethodViewModel.Event.CardRequested -> onCardRequested(event.orderId, event.installments)
            }
        }
    }
    PaymentMethodScreen(state, onBack, viewModel::selectMethod, viewModel::selectInstallments, viewModel::confirm, viewModel::retry)
}

private data class MethodOption(val method: PaymentMethod, val icon: ImageVector, val description: Int)

private val methodOptions = listOf(
    MethodOption(PaymentMethod.PIX, Icons.Outlined.QrCode2, R.string.payment_method_pix_description),
    MethodOption(PaymentMethod.BOLETO_INSTALLMENTS, Icons.Outlined.Receipt, R.string.payment_method_boleto_description),
    MethodOption(PaymentMethod.CREDIT_CARD, Icons.Outlined.CreditCard, R.string.payment_method_card_description),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaymentMethodScreen(
    state: PaymentMethodUiState,
    onBack: () -> Unit,
    onSelectMethod: (PaymentMethod) -> Unit,
    onSelectInstallments: (Int) -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
) {
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
            Text(stringResource(R.string.payment_title), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        }
        val order = state.order
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PLColor.goldMid)
            }
            order == null -> Column(
                Modifier.fillMaxSize().padding(PLSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.loadError?.asString().orEmpty(), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
                PLButton(stringResource(R.string.common_try_again), onClick = onRetry, modifier = Modifier.widthIn(max = 220.dp))
            }
            !order.canPay -> Box(Modifier.fillMaxSize().padding(PLSpacing.xl), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.payment_not_payable), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
            }
            else -> {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(PLSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
                ) {
                    Text(stringResource(R.string.orders_number, order.displayNumber), style = PLFont.body, color = PLColor.textMuted)
                    Text(order.totalAmount.brlFormatted(), style = PLFont.h1, color = PLColor.goldBright)
                    methodOptions.forEach { option ->
                        MethodCard(option, selected = state.method == option.method) { onSelectMethod(option.method) }
                    }
                    if (state.usesInstallments && state.installmentOptions.size > 1) {
                        Text(
                            stringResource(R.string.payment_installments).uppercase(),
                            style = PLFont.label.copy(letterSpacing = 1.sp),
                            color = PLColor.textMuted,
                            modifier = Modifier.padding(top = PLSpacing.sm),
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
                            state.installmentOptions.forEach { count ->
                                FilterChip(
                                    selected = state.installments == count,
                                    onClick = { onSelectInstallments(count) },
                                    label = {
                                        Text(
                                            stringResource(R.string.payment_installment_option, count, Installments.amount(order.totalAmount, count).brlFormatted()),
                                            style = PLFont.caption,
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = PLColor.backgroundElevated,
                                        labelColor = PLColor.textPrimary,
                                        selectedContainerColor = PLColor.goldMid,
                                        selectedLabelColor = PLColor.backgroundPrimary,
                                    ),
                                )
                            }
                        }
                    }
                    state.submitError?.let { Text(it.asString(), style = PLFont.body, color = PLColor.error) }
                }
                Column(
                    Modifier
                        .background(PLColor.backgroundSecondary)
                        .navigationBarsPadding()
                        .padding(horizontal = PLSpacing.xl, vertical = PLSpacing.md),
                ) {
                    PLButton(
                        text = stringResource(
                            when (state.method) {
                                PaymentMethod.PIX -> R.string.payment_confirm_pix
                                PaymentMethod.BOLETO_INSTALLMENTS -> R.string.payment_confirm_boleto
                                else -> R.string.payment_confirm_card
                            },
                        ),
                        onClick = onConfirm,
                        type = if (state.isSubmitting) PLButtonType.Loading else PLButtonType.Primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MethodCard(option: MethodOption, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) PLColor.goldMid else PLColor.borderSubtle, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(PLSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(option.icon, contentDescription = null, tint = PLColor.goldMid, modifier = Modifier.size(28.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(option.method.labelRes), style = PLFont.button, color = PLColor.textPrimary)
            Text(stringResource(option.description), style = PLFont.caption, color = PLColor.textMuted)
        }
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = PLColor.goldBright, unselectedColor = PLColor.textDisabled))
    }
}
