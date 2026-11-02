package com.luxkasTech.PaintresLumiere.feature.payment

import androidx.lifecycle.SavedStateHandle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.data.payment.CardInput
import com.luxkasTech.PaintresLumiere.data.payment.CardTokenizationUnavailableException
import com.luxkasTech.PaintresLumiere.data.payment.CardTokenizer
import com.luxkasTech.PaintresLumiere.data.payment.Payment
import com.luxkasTech.PaintresLumiere.data.payment.PaymentMethod
import com.luxkasTech.PaintresLumiere.data.payment.PaymentRepository
import com.luxkasTech.PaintresLumiere.data.payment.PaymentStatus
import com.luxkasTech.PaintresLumiere.testing.MainDispatcherRule
import com.luxkasTech.PaintresLumiere.testing.testPayment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CardPaymentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tokenized = mutableListOf<CardInput>()
    private var tokenizerFails = false
    private val tokenizer = object : CardTokenizer {
        override suspend fun tokenize(card: CardInput): String {
            if (tokenizerFails) throw CardTokenizationUnavailableException()
            tokenized += card
            return "tok_123"
        }
    }
    private var resultStatus = PaymentStatus.CONFIRMED
    private val sent = mutableListOf<Pair<Int?, String?>>()
    private val payments = object : PaymentRepository {
        override suspend fun createPayment(orderId: String, method: PaymentMethod, installmentCount: Int?, cardToken: String?): Payment {
            sent += installmentCount to cardToken
            return testPayment(method, resultStatus)
        }
    }
    private val vm by lazy { CardPaymentViewModel(SavedStateHandle(mapOf("orderId" to "o1", "installments" to 3)), tokenizer, payments) }

    private fun fillValid() {
        vm.onNumberChange("4242 4242 4242 4242")
        vm.onHolderChange("LUCAS C")
        vm.onExpiryChange("12/30")
        vm.onCvvChange("123")
    }

    @Test
    fun `invalid fields are flagged and nothing is tokenized`() {
        vm.onNumberChange("4242424242424241")
        vm.pay()
        assertEquals(setOf(CardField.Number, CardField.Holder, CardField.Expiry, CardField.Cvv), vm.state.value.errors)
        assertTrue(tokenized.isEmpty())
    }

    @Test
    fun `only the token and installments reach the API, and card data is cleared`() = runTest {
        fillValid()
        vm.pay()
        assertEquals(listOf<Pair<Int?, String?>>(3 to "tok_123"), sent)
        assertEquals(CardPaymentViewModel.Event.Paid("o1"), vm.events.first())
        assertEquals("", vm.state.value.number)
        assertEquals("", vm.state.value.cvv)
    }

    @Test
    fun `a decline keeps the form, clears the cvv and offers another method`() {
        resultStatus = PaymentStatus.FAILED
        fillValid()
        vm.pay()
        assertEquals(UiText.Resource(R.string.card_declined), vm.state.value.failure)
        assertEquals("", vm.state.value.cvv)
        assertEquals("4242424242424242", vm.state.value.number)
    }

    @Test
    fun `no tokenizer in this build is explained`() {
        tokenizerFails = true
        fillValid()
        vm.pay()
        assertEquals(UiText.Resource(R.string.card_unavailable), vm.state.value.failure)
        assertTrue(sent.isEmpty())
    }
}
