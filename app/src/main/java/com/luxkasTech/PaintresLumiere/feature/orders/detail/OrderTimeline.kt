package com.luxkasTech.PaintresLumiere.feature.orders.detail

import com.luxkasTech.PaintresLumiere.data.order.OrderStatus
import com.luxkasTech.PaintresLumiere.data.order.OrderStatusEntry
import java.time.Instant

enum class StepState { Done, Current, Upcoming, Cancelled }

data class TimelineStep(
    val status: OrderStatus,
    val state: StepState,
    val at: Instant? = null,
    val note: String? = null,
)

/**
 * Builds the tracking timeline from `status_history`:
 * - every happy-path step (recebido → … → entregue) reached so far is [StepState.Done], except the
 *   latest one, which is [StepState.Current] — or Done too once the order is delivered;
 * - steps not reached yet are [StepState.Upcoming];
 * - a cancelled order stops at the last reached step and ends with a [StepState.Cancelled] step;
 *   nothing after it is shown.
 */
fun buildTimeline(status: OrderStatus, history: List<OrderStatusEntry>): List<TimelineStep> {
    val entryByStatus = history.sortedBy { it.createdAt }.associateBy { it.status }
    val reached = OrderStatus.timeline.filter { it in entryByStatus }
    val last = reached.maxByOrNull { it.step }
    val cancelled = status == OrderStatus.CANCELLED || OrderStatus.CANCELLED in entryByStatus

    val steps = OrderStatus.timeline.mapNotNull { step ->
        val entry = entryByStatus[step]
        when {
            entry != null && step == last && !cancelled && status != OrderStatus.DELIVERED ->
                TimelineStep(step, StepState.Current, entry.createdAt, entry.note)
            entry != null -> TimelineStep(step, StepState.Done, entry.createdAt, entry.note)
            cancelled -> null
            else -> TimelineStep(step, StepState.Upcoming)
        }
    }
    if (!cancelled) return steps
    val cancel = entryByStatus[OrderStatus.CANCELLED]
    return steps + TimelineStep(OrderStatus.CANCELLED, StepState.Cancelled, cancel?.createdAt, cancel?.note)
}
