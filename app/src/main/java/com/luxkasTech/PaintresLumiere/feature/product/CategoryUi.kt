package com.luxkasTech.PaintresLumiere.feature.product

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.ui.graphics.vector.ImageVector
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory

/** Icon shown on a category tile when the API sends no representative image. */
val ProductCategory.icon: ImageVector
    get() = when (this) {
        ProductCategory.NAMES -> Icons.Outlined.TextFields
        ProductCategory.NUMBERS -> Icons.Outlined.Pin
        ProductCategory.IMAGES -> Icons.Outlined.Image
        ProductCategory.LETTERS -> Icons.Outlined.SortByAlpha
        ProductCategory.UNKNOWN -> Icons.Outlined.Category
    }
