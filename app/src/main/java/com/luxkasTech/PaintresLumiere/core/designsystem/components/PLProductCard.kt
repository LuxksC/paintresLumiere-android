package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import java.math.BigDecimal

/**
 * Display data for [PLProductCard]. Kept free of domain types so the Design System doesn't depend
 * on the data layer; features map their models into it.
 *
 * @param discountLabel e.g. `-15%`, or null when there is no discount.
 * @param unavailableLabel e.g. `ESGOTADO`, or null when the product can be bought.
 */
data class PLProductCardData(
    val name: String,
    val imageUrl: String?,
    val finalPrice: BigDecimal,
    val originalPrice: BigDecimal?,
    val discountLabel: String?,
    val unavailableLabel: String?,
)

/** Catalog tile — mirrors `PLProductCard` on iOS (square image, badges, name, price row). */
@Composable
fun PLProductCard(
    data: PLProductCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, tween(180), label = "cardScale")
    val shape = RoundedCornerShape(PLRadius.card)
    val a11yPrice = data.originalPrice?.let {
        stringResource(R.string.ds_product_card_price_discounted_a11y, data.finalPrice.brlFormatted(), it.brlFormatted())
    } ?: data.finalPrice.brlFormatted()
    // The custom description replaces the children's text for TalkBack, so it must also carry the
    // unavailable label; otherwise a sold-out product is announced as buyable.
    val a11yDescription = listOfNotNull(data.name, a11yPrice, data.unavailableLabel).joinToString(", ")

    Column(
        verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(1.dp, PLColor.borderSubtle, shape)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = a11yDescription }
            .padding(PLSpacing.sm),
    ) {
        val imageShape = RoundedCornerShape(PLRadius.card - 2.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(imageShape),
        ) {
            PLRemoteImage(url = data.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
            if (data.discountLabel != null) {
                Text(
                    text = data.discountLabel,
                    style = PLFont.label,
                    color = PLColor.backgroundPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(PLSpacing.sm)
                        .background(PLColor.goldBright, RoundedCornerShape(PLRadius.badge))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            if (data.unavailableLabel != null) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = data.unavailableLabel.uppercase(),
                        style = PLFont.label.copy(letterSpacing = 1.sp),
                        color = PLColor.textPrimary,
                        modifier = Modifier
                            .background(PLColor.backgroundPrimary.copy(alpha = 0.7f), RoundedCornerShape(PLRadius.badge))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
            Text(
                text = data.name,
                style = PLFont.button,
                color = PLColor.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            PLProductPriceRow(finalPrice = data.finalPrice, originalPrice = data.originalPrice)
        }
    }
}

/** Final price in gold, with the original price struck through when discounted. */
@Composable
fun PLProductPriceRow(
    finalPrice: BigDecimal,
    originalPrice: BigDecimal?,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(if (large) PLSpacing.sm else 6.dp),
    ) {
        Text(
            text = finalPrice.brlFormatted(),
            style = if (large) PLFont.h2 else PLFont.button,
            color = PLColor.goldBright,
        )
        if (originalPrice != null) {
            Text(
                text = originalPrice.brlFormatted(),
                style = (if (large) PLFont.body else PLFont.caption).copy(textDecoration = TextDecoration.LineThrough),
                color = PLColor.textMuted,
            )
        }
    }
}

@Preview(backgroundColor = 0xFF0C0907, showBackground = true, widthDp = 400)
@Composable
private fun PLProductCardPreview() {
    Row(Modifier.padding(PLSpacing.md), horizontalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
        PLProductCard(
            data = PLProductCardData("Luna", null, BigDecimal("42.50"), BigDecimal("50.00"), "-15%", null),
            onClick = {},
            modifier = Modifier.width(170.dp),
        )
        PLProductCard(
            data = PLProductCardData("Nome personalizado com texto longo", null, BigDecimal("19.90"), null, null, "Esgotado"),
            onClick = {},
            modifier = Modifier.width(170.dp),
        )
    }
}
