package com.luxkasTech.PaintresLumiere.core.messages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import kotlin.math.min
import kotlin.math.roundToInt

/** Renders the [MessagesService] queue at the top of the window. Place once, above the NavHost. */
@Composable
fun ToastHost(messages: MessagesService, modifier: Modifier = Modifier) {
    val current by messages.current.collectAsStateWithLifecycle()
    // Keep the last message around (without triggering recomposition) so the exit animation still
    // has something to draw after `current` turns null.
    val lastShown = remember { LastShown() }
    current?.let { lastShown.message = it }

    AnimatedVisibility(
        visible = current != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = PLSpacing.md, vertical = PLSpacing.xs),
    ) {
        lastShown.message?.let { Toast(it, onDismiss = messages::dismissCurrent) }
    }
}

private class LastShown {
    var message: ToastMessage? = null
}

@Composable
private fun Toast(message: ToastMessage, onDismiss: () -> Unit) {
    var dragOffset by remember(message.id) { mutableFloatStateOf(0f) }
    val shape = RoundedCornerShape(PLRadius.card)
    Row(
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, min(dragOffset, 0f).roundToInt()) }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { dragOffset += it },
                onDragStopped = { if (dragOffset < -120f) onDismiss() else dragOffset = 0f },
            )
            .shadow(12.dp, shape)
            .background(message.type.background, shape)
            .border(1.dp, message.type.foreground.copy(alpha = 0.4f), shape)
            .padding(horizontal = PLSpacing.md, vertical = PLSpacing.sm + 2.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        Icon(
            imageVector = message.iconVector,
            contentDescription = null,
            tint = message.type.foreground,
            modifier = Modifier.size(24.dp),
        )
        Column(Modifier.align(Alignment.CenterVertically)) {
            Text(message.title.asString(), style = PLFont.button, color = PLColor.textPrimary)
            message.subtitle?.let {
                Text(it.asString(), style = PLFont.caption, color = PLColor.textMuted, maxLines = 2)
            }
        }
    }
}

private val ToastType.foreground: Color
    get() = when (this) {
        ToastType.Success -> PLColor.success
        ToastType.Warning -> PLColor.warning
        ToastType.Error -> PLColor.error
    }

private val ToastType.background: Color
    get() = when (this) {
        ToastType.Success -> PLColor.successBackground
        ToastType.Warning -> PLColor.warningBackground
        ToastType.Error -> PLColor.errorBackground
    }

private val ToastMessage.iconVector: ImageVector
    get() = when (icon) {
        ToastIcon.Cart -> Icons.Filled.ShoppingBag
        null -> when (type) {
            ToastType.Success -> Icons.Filled.CheckCircle
            ToastType.Warning -> Icons.Filled.Warning
            ToastType.Error -> Icons.Filled.Cancel
        }
    }

@Preview(backgroundColor = 0xFF0C0907, showBackground = true)
@Composable
private fun ToastPreview() {
    Column(Modifier.padding(PLSpacing.md), verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        Toast(ToastMessage(1, UiText.Raw("Adicionado ao carrinho"), UiText.Raw("Luna · Dourado"), ToastType.Success, ToastIcon.Cart)) {}
        Toast(ToastMessage(2, UiText.Raw("Em breve"), UiText.Raw("Esta funcionalidade está em construção."), ToastType.Warning)) {}
        Toast(ToastMessage(3, UiText.Raw("Sem conexão"), UiText.Raw("Verifique sua internet e tente novamente."), ToastType.Error)) {}
    }
}
