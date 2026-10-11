package com.luxkasTech.PaintresLumiere.feature.product

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLProductCardData
import com.luxkasTech.PaintresLumiere.data.product.Product

/** Maps a catalog [Product] to the Design System's display model. */
@Composable
fun Product.toCardData(): PLProductCardData = PLProductCardData(
    name = name,
    imageUrl = primaryImageUrl,
    finalPrice = pricing.finalPrice,
    originalPrice = pricing.sellingPrice.takeIf { pricing.hasDiscount },
    discountLabel = pricing.discountPercentLabel.takeIf { pricing.hasDiscount },
    unavailableLabel = if (status.isPurchasable) null else stringResource(status.labelRes),
)
