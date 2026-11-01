package com.luxkasTech.PaintresLumiere.feature.payment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.format.formattedDate
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

@Composable
fun BoletoPaymentRoute(
    onBack: () -> Unit,
    onViewOrder: (orderId: String) -> Unit,
    viewModel: PendingPaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.startPolling()
        onPauseOrDispose { viewModel.stopPolling() }
    }
    BoletoPaymentScreen(state, onBack, viewModel::checkNow) { onViewOrder(state.orderId) }
}

@Composable
fun BoletoPaymentScreen(state: PendingPaymentUiState, onBack: () -> Unit, onCheckNow: () -> Unit, onViewOrder: () -> Unit) {
    val payment = state.payment
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val copied = stringResource(R.string.boleto_line_copied)
    val shareTitle = stringResource(R.string.boleto_share)
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
            Text(stringResource(R.string.payment_method_boleto), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(PLSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
        ) {
            if (state.isPaid) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PLColor.success, modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally))
                Text(stringResource(R.string.payment_paid_title), style = PLFont.h1, color = PLColor.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.payment_paid_message), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                PLButton(stringResource(R.string.confirmation_view_order), onClick = onViewOrder)
                return@Column
            }
            val count = payment.installmentCount ?: 1
            val shape = RoundedCornerShape(PLRadius.card)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(PLColor.backgroundElevated, shape)
                    .border(1.dp, PLColor.borderSubtle, shape)
                    .padding(PLSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(PLSpacing.xs),
            ) {
                Text(payment.amount.brlFormatted(), style = PLFont.h1, color = PLColor.goldBright)
                payment.installmentAmount?.let {
                    Text(stringResource(R.string.payment_installment_option, count, it.brlFormatted()), style = PLFont.button, color = PLColor.textPrimary)
                }
                payment.dueDate?.let {
                    Text(stringResource(R.string.boleto_first_due, it.formattedDate()), style = PLFont.caption, color = PLColor.textMuted)
                }
                payment.paidInstallments?.let { paid ->
                    LinearProgressIndicator(
                        progress = { paid.toFloat() / count },
                        color = PLColor.success,
                        trackColor = PLColor.backgroundHighlight,
                        modifier = Modifier.fillMaxWidth().padding(top = PLSpacing.sm),
                    )
                    Text(stringResource(R.string.boleto_paid_progress, paid, count), style = PLFont.caption, color = PLColor.textMuted)
                }
            }
            payment.boletoUrl?.let { url ->
                PLButton(stringResource(R.string.boleto_open), onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: ActivityNotFoundException) {
                        android.widget.Toast.makeText(context, url, android.widget.Toast.LENGTH_LONG).show()
                    }
                })
                PLButton(stringResource(R.string.boleto_share), type = PLButtonType.Ghost, onClick = {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, url)
                    context.startActivity(Intent.createChooser(send, shareTitle))
                })
            }
            payment.boletoDigitableLine?.let { line ->
                Text(line, style = PLFont.caption, color = PLColor.textSecondary, modifier = Modifier.fillMaxWidth().background(PLColor.backgroundElevated, RoundedCornerShape(PLRadius.input)).padding(PLSpacing.md))
                PLButton(stringResource(R.string.boleto_copy_line), type = PLButtonType.Secondary, onClick = {
                    clipboard.setText(AnnotatedString(line))
                    android.widget.Toast.makeText(context, copied, android.widget.Toast.LENGTH_SHORT).show()
                })
            }
            // Production rule (backend card PL-149, recommended): starts with the first installment.
            Text(stringResource(R.string.boleto_instructions), style = PLFont.body, color = PLColor.textMuted)
            PLButton(
                stringResource(R.string.payment_check_now),
                type = if (state.isChecking) PLButtonType.Loading else PLButtonType.Ghost,
                onClick = onCheckNow,
            )
        }
    }
}
