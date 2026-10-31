package com.luxkasTech.PaintresLumiere.data.product

import androidx.annotation.StringRes
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.network.UnknownTolerantEnumSerializer
import kotlinx.serialization.Serializable

/**
 * Product category. [UNKNOWN] must never appear in category grids or filters, but a product with
 * an unknown category stays listable in the general catalog.
 */
@Serializable(with = ProductCategory.Serializer::class)
enum class ProductCategory(val raw: String, @StringRes val labelRes: Int) {
    NAMES("names", R.string.product_category_names),
    NUMBERS("numbers", R.string.product_category_numbers),
    IMAGES("images", R.string.product_category_images),
    LETTERS("letters", R.string.product_category_letters),
    UNKNOWN("", R.string.product_category_unknown);

    object Serializer : UnknownTolerantEnumSerializer<ProductCategory>(
        "ProductCategory", entries, UNKNOWN, { it.raw },
    )
}

/** Variant availability. Anything but [ACTIVE] — including [UNKNOWN] — is never purchasable. */
@Serializable(with = ProductStatus.Serializer::class)
enum class ProductStatus(val raw: String, @StringRes val labelRes: Int) {
    ACTIVE("active", R.string.product_status_active),
    INACTIVE("inactive", R.string.product_status_inactive),
    OUT_OF_STOCK("out_of_stock", R.string.product_status_out_of_stock),
    UNKNOWN("", R.string.product_status_unavailable);

    val isPurchasable: Boolean get() = this == ACTIVE

    object Serializer : UnknownTolerantEnumSerializer<ProductStatus>(
        "ProductStatus", entries, UNKNOWN, { it.raw },
    )
}

/**
 * Variant color, with the swatch used by the color selector (same hex as iOS `ProductColor`).
 * [UNKNOWN] is never offered as a selectable option and renders with a neutral swatch.
 */
@Serializable(with = ProductColor.Serializer::class)
enum class ProductColor(
    val raw: String,
    @StringRes val labelRes: Int,
    val swatchArgb: Long,
    val needsSwatchOutline: Boolean = false,
) {
    GOLD("gold", R.string.product_color_gold, 0xFFD4A53A),
    SILVER("silver", R.string.product_color_silver, 0xFFC0C5CB, needsSwatchOutline = true),
    BRONZE("bronze", R.string.product_color_bronze, 0xFF8C6239),
    ROSE("rose", R.string.product_color_rose, 0xFFE6B6B0, needsSwatchOutline = true),
    TRANSPARENT("transparent", R.string.product_color_transparent, 0x14FFFFFF, needsSwatchOutline = true),
    BLACK("black", R.string.product_color_black, 0xFF1A1A1A),
    UNKNOWN("", R.string.product_color_unknown, 0xFF6F6A62, needsSwatchOutline = true);

    object Serializer : UnknownTolerantEnumSerializer<ProductColor>(
        "ProductColor", entries, UNKNOWN, { it.raw },
    )

    companion object {
        /** Colors that may be offered in a selector — never [UNKNOWN]. */
        val selectable: List<ProductColor> = entries.filter { it != UNKNOWN }
    }
}
