package com.luxkasTech.PaintresLumiere.feature.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.data.order.OrderStatus

val OrderStatus.color: Color
    get() = when (this) {
        OrderStatus.RECEIVED -> PLColor.goldMid
        OrderStatus.IN_PRODUCTION -> PLColor.warning
        OrderStatus.READY -> PLColor.goldBright
        OrderStatus.SHIPPED -> PLColor.textSecondary
        OrderStatus.DELIVERED -> PLColor.success
        OrderStatus.CANCELLED -> PLColor.error
        OrderStatus.UNKNOWN -> PLColor.textDisabled
    }

/** Status pill used on order cards and in the order header. */
@Composable
fun OrderStatusBadge(status: OrderStatus, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(status.labelRes).uppercase(),
        style = PLFont.label.copy(letterSpacing = 0.5.sp),
        color = status.color,
        modifier = modifier
            .background(status.color.copy(alpha = 0.15f), RoundedCornerShape(PLRadius.badge))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
