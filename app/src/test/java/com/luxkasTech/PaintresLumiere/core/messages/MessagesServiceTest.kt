package com.luxkasTech.PaintresLumiere.core.messages

import com.luxkasTech.PaintresLumiere.core.common.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesServiceTest {

    private fun TestScope.service() = DefaultMessagesService(backgroundScope)

    private fun DefaultMessagesService.showRaw(title: String) =
        show(UiText.Raw(title), type = ToastType.Success)

    @Test
    fun `shows the first message immediately`() = runTest {
        val service = service()
        service.showRaw("one")
        assertEquals(UiText.Raw("one"), service.current.value?.title)
    }

    @Test
    fun `queues messages and shows them one at a time`() = runTest {
        val service = service()
        service.showRaw("one")
        service.showRaw("two")
        assertEquals(UiText.Raw("one"), service.current.value?.title)

        advanceTimeBy(MessagesService.DISPLAY_MILLIS + 1)
        assertNull(service.current.value)

        advanceTimeBy(MessagesService.GAP_MILLIS)
        runCurrent()
        assertEquals(UiText.Raw("two"), service.current.value?.title)
    }

    @Test
    fun `dismissCurrent hides now and moves on to the next`() = runTest {
        val service = service()
        service.showRaw("one")
        service.showRaw("two")
        service.dismissCurrent()
        assertNull(service.current.value)

        advanceTimeBy(MessagesService.GAP_MILLIS + 1)
        assertEquals(UiText.Raw("two"), service.current.value?.title)
    }

    @Test
    fun `auto dismiss of a dismissed message does not hide the next one early`() = runTest {
        val service = service()
        service.showRaw("one")
        advanceTimeBy(1_000)
        service.dismissCurrent()
        service.showRaw("two")
        advanceTimeBy(MessagesService.GAP_MILLIS + 1)
        assertEquals(UiText.Raw("two"), service.current.value?.title)

        // "one"'s original timer would have fired here; "two" must still be visible.
        advanceTimeBy(MessagesService.DISPLAY_MILLIS - 1_500)
        assertEquals(UiText.Raw("two"), service.current.value?.title)
    }
}
