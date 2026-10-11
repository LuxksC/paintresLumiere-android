package com.luxkasTech.PaintresLumiere.feature.orders.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.format.formattedDateTime
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing

/** Vertical tracking timeline: done steps in gold, current step highlighted, future steps muted. */
@Composable
fun OrderTimelineView(steps: List<TimelineStep>, modifier: Modifier = Modifier) {
    Column(modifier) {
        steps.forEachIndexed { index, step ->
            TimelineRow(step, isLast = index == steps.lastIndex, nextReached = steps.getOrNull(index + 1)?.state.let { it == StepState.Done || it == StepState.Current || it == StepState.Cancelled })
        }
    }
}

@Composable
private fun TimelineRow(step: TimelineStep, isLast: Boolean, nextReached: Boolean) {
    val label = stringResource(step.status.labelRes)
    val stateLabel = stringResource(
        when (step.state) {
            StepState.Done -> R.string.order_step_done
            StepState.Current -> R.string.order_step_current
            StepState.Upcoming -> R.string.order_step_upcoming
            StepState.Cancelled -> R.string.order_status_cancelled
        },
    )
    Row(
        Modifier
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) { contentDescription = "$label, $stateLabel" },
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxHeight()) {
            Dot(step.state)
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(if (nextReached) PLColor.goldMid else PLColor.borderSubtle),
                )
            }
        }
        Column(Modifier.padding(bottom = if (isLast) 0.dp else PLSpacing.lg)) {
            Text(
                label,
                style = if (step.state == StepState.Current) PLFont.h2 else PLFont.button,
                color = when (step.state) {
                    StepState.Upcoming -> PLColor.textDisabled
                    StepState.Cancelled -> PLColor.error
                    else -> PLColor.textPrimary
                },
            )
            step.at?.let { Text(it.formattedDateTime(), style = PLFont.caption, color = PLColor.textMuted) }
            step.note?.let { Text(it, style = PLFont.body, color = PLColor.textSecondary, modifier = Modifier.padding(top = PLSpacing.xs)) }
        }
    }
}

@Composable
private fun Dot(state: StepState) {
    val (fill, border) = when (state) {
        StepState.Done -> PLColor.goldMid to PLColor.goldMid
        StepState.Current -> PLColor.goldBright to PLColor.goldBright
        StepState.Upcoming -> Color.Transparent to PLColor.borderSubtle
        StepState.Cancelled -> PLColor.error to PLColor.error
    }
    Box(
        Modifier
            .size(if (state == StepState.Current) 24.dp else 20.dp)
            .background(fill, CircleShape)
            .border(2.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            StepState.Done -> Icon(Icons.Filled.Check, contentDescription = null, tint = PLColor.backgroundPrimary, modifier = Modifier.size(14.dp))
            StepState.Cancelled -> Icon(Icons.Filled.Close, contentDescription = null, tint = PLColor.textPrimary, modifier = Modifier.size(14.dp))
            else -> Unit
        }
    }
}
