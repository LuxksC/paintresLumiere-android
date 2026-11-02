package com.luxkasTech.PaintresLumiere.data.payment

import androidx.annotation.StringRes
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.network.BigDecimalSerializer
import com.luxkasTech.PaintresLumiere.core.network.InstantSerializer
import com.luxkasTech.PaintresLumiere.core.network.UnknownTolerantEnumSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.Instant

/** Payment methods from backend card PL-144 (Asaas). */
@Serializable(with = PaymentMethod.Serializer::class)
enum class PaymentMethod(val raw: String, @StringRes val labelRes: Int) {
    PIX("pix", R.string.payment_method_pix),
    BOLETO_INSTALLMENTS("boleto_parcelado", R.string.payment_method_boleto),
    CREDIT_CARD("credit_card", R.string.payment_method_card),
    UNKNOWN("", R.string.payment_method_unknown);

    object Serializer : UnknownTolerantEnumSerializer<PaymentMethod>("PaymentMethod", entries, UNKNOWN, { it.raw })
}

/** Provider lifecycle from backend card PL-144. */
@Serializable(with = PaymentStatus.Serializer::class)
enum class PaymentStatus(val raw: String, @StringRes val labelRes: Int) {
    PENDING("pendente", R.string.payment_status_pending),
    CONFIRMED("confirmado", R.string.payment_status_confirmed),
    RECEIVED("recebido", R.string.payment_status_received),
    OVERDUE("vencido", R.string.payment_status_overdue),
    REFUNDED("estornado", R.string.payment_status_refunded),
    CANCELLED("cancelado", R.string.payment_status_cancelled),
    FAILED("falhou", R.string.payment_status_failed),
    UNKNOWN("", R.string.payment_status_unknown);

    val isPaid: Boolean get() = this == CONFIRMED || this == RECEIVED

    object Serializer : UnknownTolerantEnumSerializer<PaymentStatus>("PaymentStatus", entries, UNKNOWN, { it.raw })
}

/**
 * A charge for an order. An order can have several (overdue boleto then pix, declined card then
 * boleto) — the latest one is the relevant one.
 */
@Serializable
data class Payment(
    val id: String,
    val method: PaymentMethod = PaymentMethod.UNKNOWN,
    val status: PaymentStatus = PaymentStatus.UNKNOWN,
    @Serializable(with = BigDecimalSerializer::class) val amount: BigDecimal,
    @SerialName("installment_count") val installmentCount: Int? = null,
    @SerialName("installment_amount") @Serializable(with = BigDecimalSerializer::class) val installmentAmount: BigDecimal? = null,
    @SerialName("paid_installments") val paidInstallments: Int? = null,
    @SerialName("due_date") @Serializable(with = InstantSerializer::class) val dueDate: Instant? = null,
    @SerialName("boleto_url") val boletoUrl: String? = null,
    @SerialName("boleto_digitable_line") val boletoDigitableLine: String? = null,
    /** PNG, base64-encoded. */
    @SerialName("pix_qr_code") val pixQrCode: String? = null,
    @SerialName("pix_copy_paste") val pixCopyPaste: String? = null,
    @SerialName("card_brand") val cardBrand: String? = null,
    @SerialName("card_last4") val cardLast4: String? = null,
    @SerialName("created_at") @Serializable(with = InstantSerializer::class) val createdAt: Instant? = null,
)

/**
 * Body of `POST /orders/{id}/payment`. Never carries an amount (always recomputed server-side) and
 * never raw card data — only the token produced on the device.
 */
@Serializable
data class CreatePaymentRequest(
    val method: PaymentMethod,
    val installmentCount: Int? = null,
    val cardToken: String? = null,
)
