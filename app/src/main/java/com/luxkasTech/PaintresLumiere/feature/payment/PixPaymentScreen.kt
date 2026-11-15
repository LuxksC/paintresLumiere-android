package com.luxkasTech.PaintresLumiere.feature.payment

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.format.formattedDateTime
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

@Composable
fun PixPaymentRoute(
    onBack: () -> Unit,
    onViewOrder: (orderId: String) -> Unit,
    viewModel: PendingPaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.startPolling()
        onPauseOrDispose { viewModel.stopPolling() }
    }
    PixPaymentScreen(state, onBack, viewModel::checkNow) { onViewOrder(state.orderId) }
}

@Composable
fun PixPaymentScreen(state: PendingPaymentUiState, onBack: () -> Unit, onCheckNow: () -> Unit, onViewOrder: () -> Unit) {
    val payment = state.payment
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedMessage = stringResource(R.string.pix_copied)
    val shareTitle = stringResource(R.string.pix_share)
    val qr = remember(payment.pixQrCode) {
        payment.pixQrCode?.let { encoded ->
            runCatching {
                val bytes = Base64.decode(encoded, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        }
    }
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
            Text(stringResource(R.string.payment_method_pix), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(PLSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
        ) {
            if (state.isPaid) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PLColor.success, modifier = Modifier.size(64.dp))
                Text(stringResource(R.string.payment_paid_title), style = PLFont.h1, color = PLColor.textPrimary, textAlign = TextAlign.Center)
                Text(stringResource(R.string.payment_paid_message), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
                PLButton(stringResource(R.string.confirmation_view_order), onClick = onViewOrder)
                return@Column
            }
            Text(payment.amount.brlFormatted(), style = PLFont.h1, color = PLColor.goldBright)
            payment.dueDate?.let {
                Text(stringResource(R.string.payment_due, it.formattedDateTime()), style = PLFont.caption, color = PLColor.textMuted)
            }
            if (qr != null) {
                Box(
                    Modifier
                        .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(PLRadius.card))
                        .padding(PLSpacing.sm),
                ) {
                    Image(
                        bitmap = qr,
                        contentDescription = stringResource(R.string.pix_qr_a11y),
                        filterQuality = FilterQuality.None,
                        modifier = Modifier.size(220.dp),
                    )
                }
            }
            payment.pixCopyPaste?.let { code ->
                Text(
                    code,
                    style = PLFont.caption,
                    color = PLColor.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PLColor.backgroundElevated, RoundedCornerShape(PLRadius.input))
                        .padding(PLSpacing.md),
                )
                // Most people pay on the same phone — they can't scan their own screen.
                PLButton(stringResource(R.string.pix_copy), onClick = {
                    clipboard.setText(AnnotatedString(code))
                    android.widget.Toast.makeText(context, copiedMessage, android.widget.Toast.LENGTH_SHORT).show()
                })
                PLButton(stringResource(R.string.pix_share), type = PLButtonType.Ghost, onClick = {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, code)
                    context.startActivity(Intent.createChooser(send, shareTitle))
                })
            }
            Text(stringResource(R.string.pix_instructions), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
            PLButton(
                stringResource(R.string.payment_check_now),
                type = if (state.isChecking) PLButtonType.Loading else PLButtonType.Secondary,
                onClick = onCheckNow,
            )
        }
    }
}
