package com.luxkasTech.PaintresLumiere.core.telemetry

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crash reporting. The provider (Firebase Crashlytics or Sentry) needs project credentials that
 * don't exist yet (`google-services.json` / DSN), so the app depends on this interface and
 * [LogcatTelemetry] stands in. Adding a provider = one new implementation bound in `TelemetryModule`.
 *
 * Never pass personal data (email, CPF/CNPJ, card data, tokens) to any of these calls.
 */
interface CrashReporter {
    fun recordNonFatal(throwable: Throwable, context: Map<String, String> = emptyMap())
    fun recordFatal(throwable: Throwable)
    /** Opaque user id only — never email or name. */
    fun setUserId(id: String?)
}

/** Product analytics: screen views and a small, explicit set of business events. */
interface Analytics {
    fun screenView(screen: String)
    fun event(event: AnalyticsEvent)
}

sealed class AnalyticsEvent(val name: String, val params: Map<String, String> = emptyMap()) {
    data object Login : AnalyticsEvent("login")
    data object SignUp : AnalyticsEvent("sign_up")
    data class AddToCart(val productId: String, val quantity: Int) :
        AnalyticsEvent("add_to_cart", mapOf("product_id" to productId, "quantity" to quantity.toString()))
    data class OrderPlaced(val orderId: String) : AnalyticsEvent("order_placed", mapOf("order_id" to orderId))
    data class PaymentStarted(val method: String) : AnalyticsEvent("payment_started", mapOf("method" to method))
}

@Singleton
class LogcatTelemetry @Inject constructor() : CrashReporter, Analytics {
    override fun recordNonFatal(throwable: Throwable, context: Map<String, String>) {
        Log.w(TAG, "non-fatal $context", throwable)
    }

    override fun recordFatal(throwable: Throwable) {
        Log.e(TAG, "fatal", throwable)
    }

    override fun setUserId(id: String?) {
        Log.d(TAG, "user=${id ?: "-"}")
    }

    override fun screenView(screen: String) {
        Log.d(TAG, "screen $screen")
    }

    override fun event(event: AnalyticsEvent) {
        Log.d(TAG, "event ${event.name} ${event.params}")
    }

    private companion object {
        const val TAG = "Telemetry"
    }
}

/**
 * Records uncaught exceptions, then lets the previous handler (the system's) crash the app as
 * usual. Installed once from the Application.
 */
class CrashHandler(
    private val reporter: CrashReporter,
    private val previous: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching { reporter.recordFatal(throwable) }
        previous?.uncaughtException(thread, throwable)
    }

    companion object {
        fun install(reporter: CrashReporter) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(reporter, Thread.getDefaultUncaughtExceptionHandler()))
        }
    }
}

/** Screen name for analytics from a type-safe route (`ProductDetailsDestination` → `ProductDetails`). */
fun screenNameOf(route: String?): String =
    route?.substringAfterLast('.')?.substringBefore('/')?.substringBefore('?')?.removeSuffix("Destination") ?: "Unknown"
