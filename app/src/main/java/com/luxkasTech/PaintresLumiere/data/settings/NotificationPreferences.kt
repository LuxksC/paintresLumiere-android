package com.luxkasTech.PaintresLumiere.data.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class NotificationTopic(val key: String) { Catalog("catalog"), OrderStatus("order_status"), Promotions("promotions") }

/**
 * Notification preferences. **Known limitation:** there is no push backend yet, so they are only
 * stored on the device (iOS does the same with UserDefaults); they move to the server with push.
 */
interface NotificationPreferences {
    val enabled: StateFlow<Set<NotificationTopic>>
    fun set(topic: NotificationTopic, on: Boolean)
}

@Singleton
class LocalNotificationPreferences @Inject constructor(
    @ApplicationContext context: Context,
) : NotificationPreferences {

    private val prefs = context.getSharedPreferences("pl_notification_prefs", Context.MODE_PRIVATE)
    private val _enabled = MutableStateFlow(
        // Order updates default to on: they are the reason to have the app.
        NotificationTopic.entries.filter { prefs.getBoolean(it.key, it == NotificationTopic.OrderStatus) }.toSet(),
    )
    override val enabled: StateFlow<Set<NotificationTopic>> = _enabled.asStateFlow()

    override fun set(topic: NotificationTopic, on: Boolean) {
        prefs.edit().putBoolean(topic.key, on).apply()
        _enabled.value = if (on) _enabled.value + topic else _enabled.value - topic
    }
}
