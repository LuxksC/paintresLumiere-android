package com.luxkasTech.PaintresLumiere.core.messages

import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide toast queue — the Android counterpart of iOS `MessagesServiceProtocol`. Messages are
 * shown one at a time at the top of the screen for [DISPLAY_MILLIS], in the order they were
 * requested. Call from the main thread.
 */
interface MessagesService {
    val current: StateFlow<ToastMessage?>

    fun show(title: UiText, subtitle: UiText? = null, type: ToastType, icon: ToastIcon? = null)
    fun dismissCurrent()

    fun showNetworkError() = show(
        UiText.Resource(R.string.toast_network_error_title),
        UiText.Resource(R.string.toast_network_error_subtitle),
        ToastType.Error,
    )

    fun showGenericError() = show(
        UiText.Resource(R.string.toast_generic_error_title),
        UiText.Resource(R.string.toast_generic_error_subtitle),
        ToastType.Error,
    )

    fun showAddedToCart(productName: String) = show(
        UiText.Resource(R.string.toast_added_to_cart_title),
        UiText.Raw(productName),
        ToastType.Success,
        ToastIcon.Cart,
    )

    fun showComingSoon() = show(
        UiText.Resource(R.string.toast_coming_soon_title),
        UiText.Resource(R.string.toast_coming_soon_subtitle),
        ToastType.Warning,
    )

    companion object {
        const val DISPLAY_MILLIS = 3_000L
        const val GAP_MILLIS = 250L
    }
}

@Singleton
class DefaultMessagesService @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
) : MessagesService {

    private val queue = ArrayDeque<ToastMessage>()
    private val _current = MutableStateFlow<ToastMessage?>(null)
    override val current: StateFlow<ToastMessage?> = _current.asStateFlow()

    private var dismissJob: Job? = null
    private var nextId = 0L

    override fun show(title: UiText, subtitle: UiText?, type: ToastType, icon: ToastIcon?) {
        queue.addLast(ToastMessage(nextId++, title, subtitle, type, icon))
        showNextIfIdle()
    }

    override fun dismissCurrent() {
        dismissJob?.cancel()
        dismissJob = null
        _current.value = null
        scope.launch {
            delay(MessagesService.GAP_MILLIS)
            showNextIfIdle()
        }
    }

    private fun showNextIfIdle() {
        if (_current.value != null || queue.isEmpty()) return
        val next = queue.removeFirst()
        _current.value = next
        dismissJob = scope.launch {
            delay(MessagesService.DISPLAY_MILLIS)
            if (_current.value?.id != next.id) return@launch
            _current.value = null
            delay(MessagesService.GAP_MILLIS)
            showNextIfIdle()
        }
    }
}
