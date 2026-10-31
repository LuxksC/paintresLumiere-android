package com.luxkasTech.PaintresLumiere.testing

import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastIcon
import com.luxkasTech.PaintresLumiere.core.messages.ToastMessage
import com.luxkasTech.PaintresLumiere.core.messages.ToastType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Records every toast; the convenience methods resolve through the interface defaults. */
class FakeMessagesService : MessagesService {
    val shown = mutableListOf<ToastMessage>()
    override val current: StateFlow<ToastMessage?> = MutableStateFlow(null)

    override fun show(title: UiText, subtitle: UiText?, type: ToastType, icon: ToastIcon?) {
        shown += ToastMessage(shown.size.toLong(), title, subtitle, type, icon)
    }

    override fun dismissCurrent() = Unit
}
