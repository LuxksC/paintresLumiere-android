package com.luxkasTech.PaintresLumiere.feature.orders.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.format.formattedDate
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.data.order.OrderDetail
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus

/** What the payment section offers for an order. Pure, so it is unit-tested. */
sealed interface PaymentAction {
    /** Resume the existing charge (pix QR / carnê) — never create a new one for it. */
    data class Resume(val payment: Payment) : PaymentAction
    /** No charge yet, or the last one is overdue / failed / cancelled: choose a method. */
    data object ChooseMethod : PaymentAction
    data object None : PaymentAction
}

fun paymentActionFor(order: OrderDetail): PaymentAction {
    if (!order.canPay) return PaymentAction.None
    val latest = order.latestPayment ?: return PaymentAction.ChooseMethod
    return when {
        latest.status == PaymentStatus.PENDING &&
            (latest.method == PaymentMethod.PIX || latest.method == PaymentMethod.BOLETO_INSTALLMENTS) -> PaymentAction.Resume(latest)
        latest.status == PaymentStatus.PENDING -> PaymentAction.None
        else -> PaymentAction.ChooseMethod
    }
}

@Composable
internal fun PaymentSectionContent(
    order: OrderDetail,
    onResume: (Payment) -> Unit,
    onChooseMethod: () -> Unit,
) {
    val latest = order.latestPayment
    Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        if (latest == null) {
            Text(stringResource(R.string.order_payment_none), style = PLFont.body, color = PLColor.textMuted)
        } else {
            PaymentLine(latest, highlight = true)
            if (order.payments.size > 1) {
                HorizontalDivider(color = PLColor.borderSubtle)
                Text(stringResource(R.string.order_payment_history), style = PLFont.caption, color = PLColor.textMuted)
                order.payments.dropLast(1).asReversed().forEach { PaymentLine(it, highlight = false) }
            }
        }
        when (val action = paymentActionFor(order)) {
            is PaymentAction.Resume -> PLButton(
                stringResource(if (action.payment.method == PaymentMethod.PIX) R.string.order_payment_open_pix else R.string.order_payment_open_boleto),
                onClick = { onResume(action.payment) },
            )
            PaymentAction.ChooseMethod -> PLButton(
                stringResource(if (latest == null) R.string.confirmation_pay_now else R.string.order_payment_try_another),
                onClick = onChooseMethod,
                type = if (latest == null) PLButtonType.Primary else PLButtonType.Secondary,
            )
            PaymentAction.None -> Unit
        }
    }
}

@Composable
private fun PaymentLine(payment: Payment, highlight: Boolean) {
    Row {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(payment.method.labelRes) + (payment.cardLast4?.let { " •••• $it" } ?: ""),
                style = if (highlight) PLFont.button else PLFont.caption,
                color = PLColor.textPrimary,
            )
            val details = listOfNotNull(
                payment.installmentCount?.takeIf { it > 1 }?.let { count ->
                    payment.paidInstallments?.let { stringResource(R.string.boleto_paid_progress, it, count) }
                },
                payment.dueDate?.takeIf { payment.status == PaymentStatus.PENDING }?.let { stringResource(R.string.payment_due, it.formattedDate()) },
            )
            if (details.isNotEmpty()) Text(details.joinToString(" · "), style = PLFont.caption, color = PLColor.textMuted)
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text(payment.amount.brlFormatted(), style = PLFont.caption, color = PLColor.textSecondary)
            Text(
                stringResource(payment.status.labelRes),
                style = PLFont.caption,
                color = when {
                    payment.status.isPaid -> PLColor.success
                    payment.status == PaymentStatus.PENDING -> PLColor.warning
                    else -> PLColor.error
                },
            )
        }
    }
}
