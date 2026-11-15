package com.luxkasTech.PaintresLumiere.feature.payment

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.input.MaskVisualTransformation
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

private val CardNumberMask = MaskVisualTransformation("#### #### #### #### ###")
private val ExpiryMask = MaskVisualTransformation("##/##")

@Composable
fun CardPaymentRoute(
    onBack: () -> Unit,
    onPaid: (orderId: String) -> Unit,
    onChooseAnotherMethod: () -> Unit,
    viewModel: CardPaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { if (it is CardPaymentViewModel.Event.Paid) onPaid(it.orderId) }
    }
    SecureWindow()
    CardPaymentScreen(
        state = state,
        onBack = onBack,
        onNumberChange = viewModel::onNumberChange,
        onHolderChange = viewModel::onHolderChange,
        onExpiryChange = viewModel::onExpiryChange,
        onCvvChange = viewModel::onCvvChange,
        onPay = viewModel::pay,
        onChooseAnotherMethod = onChooseAnotherMethod,
    )
}

/** Blocks screenshots and the recents thumbnail while card data is on screen. */
@Composable
private fun SecureWindow() {
    val activity = LocalContext.current as? Activity ?: return
    DisposableEffect(activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

@Composable
fun CardPaymentScreen(
    state: CardPaymentUiState,
    onBack: () -> Unit,
    onNumberChange: (String) -> Unit,
    onHolderChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    onPay: () -> Unit,
    onChooseAnotherMethod: () -> Unit,
) {
    val invalid = stringResource(R.string.card_field_invalid)
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
            Text(stringResource(R.string.payment_method_card), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(PLSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
        ) {
            if (state.installments > 1) {
                Text(stringResource(R.string.card_installments, state.installments), style = PLFont.body, color = PLColor.textMuted)
            }
            PLInputField(
                label = stringResource(R.string.card_number) + (state.brand.label.takeIf { it.isNotEmpty() }?.let { " · $it" } ?: ""),
                value = state.number,
                onValueChange = onNumberChange,
                placeholder = "0000 0000 0000 0000",
                visualTransformation = CardNumberMask,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                errorMessage = invalid.takeIf { CardField.Number in state.errors },
            )
            PLInputField(
                label = stringResource(R.string.card_holder),
                value = state.holder,
                onValueChange = onHolderChange,
                placeholder = stringResource(R.string.card_holder_placeholder),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                errorMessage = invalid.takeIf { CardField.Holder in state.errors },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
                PLInputField(
                    label = stringResource(R.string.card_expiry),
                    value = state.expiry,
                    onValueChange = onExpiryChange,
                    placeholder = "MM/AA",
                    visualTransformation = ExpiryMask,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    errorMessage = invalid.takeIf { CardField.Expiry in state.errors },
                    modifier = Modifier.weight(1f),
                )
                PLInputField(
                    label = stringResource(R.string.card_cvv),
                    value = state.cvv,
                    onValueChange = onCvvChange,
                    isSecure = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    errorMessage = invalid.takeIf { CardField.Cvv in state.errors },
                    modifier = Modifier.weight(1f),
                )
            }
            Text(stringResource(R.string.card_security_note), style = PLFont.caption, color = PLColor.textDisabled)
            state.failure?.let { failure ->
                Text(failure.asString(), style = PLFont.body, color = PLColor.error)
                // A decline doesn't invalidate the order: offer another method.
                TextButton(onClick = onChooseAnotherMethod) {
                    Text(stringResource(R.string.card_choose_another), style = PLFont.link, color = PLColor.goldMid)
                }
            }
            PLButton(
                stringResource(R.string.card_pay),
                onClick = onPay,
                type = if (state.isSubmitting) PLButtonType.Loading else PLButtonType.Primary,
            )
        }
    }
}
