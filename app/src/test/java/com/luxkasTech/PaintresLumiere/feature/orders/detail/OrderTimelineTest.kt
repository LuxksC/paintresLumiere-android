package com.luxkasTech.PaintresLumiere.feature.orders.detail

import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus.CANCELLED
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus.DELIVERED
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus.IN_PRODUCTION
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus.READY
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus.RECEIVED
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus.SHIPPED
import com.luxkasTech.PaintresLumiere.data.order.OrderStatusEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class OrderTimelineTest {

    private fun history(vararg statuses: OrderStatus) =
        statuses.mapIndexed { i, s -> OrderStatusEntry(s, Instant.parse("2026-10-0${i + 1}T10:00:00Z"), note = "n$i") }

    private fun List<TimelineStep>.shape() = map { it.status to it.state }

    @Test
    fun `in production order has done, current and upcoming steps`() {
        val steps = buildTimeline(IN_PRODUCTION, history(RECEIVED, IN_PRODUCTION))
        assertEquals(
            listOf(
                RECEIVED to StepState.Done,
                IN_PRODUCTION to StepState.Current,
                READY to StepState.Upcoming,
                SHIPPED to StepState.Upcoming,
                DELIVERED to StepState.Upcoming,
            ),
            steps.shape(),
        )
        assertEquals("n1", steps[1].note)
    }

    @Test
    fun `delivered order is fully done`() {
        val steps = buildTimeline(DELIVERED, history(RECEIVED, IN_PRODUCTION, READY, SHIPPED, DELIVERED))
        assertEquals(List(5) { StepState.Done }, steps.map { it.state })
    }

    @Test
    fun `cancelled order stops where it was and ends with the cancellation`() {
        val steps = buildTimeline(CANCELLED, history(RECEIVED, CANCELLED))
        assertEquals(listOf(RECEIVED to StepState.Done, CANCELLED to StepState.Cancelled), steps.shape())
        assertEquals("n1", steps.last().note)
    }

    @Test
    fun `history arriving out of order is sorted by time`() {
        val entries = history(RECEIVED, IN_PRODUCTION).reversed()
        assertEquals(IN_PRODUCTION to StepState.Current, buildTimeline(IN_PRODUCTION, entries).shape()[1])
    }
}
