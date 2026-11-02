package com.luxkasTech.PaintresLumiere.feature.product.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLColorOption
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLColorSelector
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLImagePlaceholder
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLPageIndicator
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLProductPriceRow
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLQuantityStepper
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLRemoteImage
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLSizeSelector
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductDimensions
import com.luxkasTech.PaintresLumiere.data.product.ProductPricing
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant
import com.luxkasTech.PaintresLumiere.data.product.ProductVariantSpecifications
import java.math.BigDecimal

@Composable
fun ProductDetailsRoute(
    onBack: () -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProductDetailsScreen(
        state = state,
        actions = ProductDetailsActions(
            onBack = onBack,
            onRetry = viewModel::retry,
            onColorSelected = viewModel::selectColor,
            onSizeSelected = viewModel::selectSize,
            onQuantityChange = viewModel::setQuantity,
            onBuy = viewModel::buyNow,
            onAddToCart = viewModel::addToCart,
        ),
    )
}

data class ProductDetailsActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onColorSelected: (ProductColor) -> Unit = {},
    val onSizeSelected: (String) -> Unit = {},
    val onQuantityChange: (Int) -> Unit = {},
    val onBuy: () -> Unit = {},
    val onAddToCart: () -> Unit = {},
)

@Composable
fun ProductDetailsScreen(state: ProductDetailsUiState, actions: ProductDetailsActions) {
    Box(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary),
    ) {
        val variant = state.selectedVariant
        when (val load = state.loadState) {
            DetailsLoadState.Loading -> CircularProgressIndicator(color = PLColor.goldMid, modifier = Modifier.align(Alignment.Center))
            is DetailsLoadState.Failed -> DetailsError(
                title = stringResource(R.string.details_error_title),
                message = load.message.asString(),
                action = stringResource(R.string.common_try_again),
                onAction = actions.onRetry,
            )
            DetailsLoadState.Loaded -> if (variant == null) {
                DetailsError(
                    title = stringResource(R.string.details_error_title),
                    message = stringResource(R.string.details_unavailable),
                    action = stringResource(R.string.common_back),
                    onAction = actions.onBack,
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    LoadedContent(state, variant, actions, Modifier.weight(1f))
                    FooterBar(state, actions)
                }
            }
        }
        BackButton(
            onClick = actions.onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = PLSpacing.md, top = PLSpacing.sm),
        )
    }
}

@Composable
private fun LoadedContent(
    state: ProductDetailsUiState,
    variant: ProductVariant,
    actions: ProductDetailsActions,
    modifier: Modifier,
) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        ImageCarousel(variant.images, variant.name)
        Column(
            Modifier.padding(horizontal = PLSpacing.xl, vertical = PLSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.xl),
        ) {
            TitleAndPrice(variant)
            if (state.hasMultipleColors || state.hasMultipleSizes) {
                VariantSelectors(state, actions)
            }
            DescriptionAndStock(variant)
            DetailsList(variant)
        }
    }
}

@Composable
private fun ImageCarousel(images: List<String>, name: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
        if (images.isEmpty()) {
            PLImagePlaceholder(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
        } else {
            val pagerState = rememberPagerState { images.size }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            ) { page ->
                PLRemoteImage(url = images[page], contentDescription = name, modifier = Modifier.fillMaxSize())
            }
            PLPageIndicator(count = images.size, activeIndex = pagerState.currentPage)
        }
    }
}

@Composable
private fun TitleAndPrice(variant: ProductVariant) {
    Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
            Text(variant.name, style = PLFont.h1, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
            PLProductPriceRow(
                finalPrice = variant.pricing.finalPrice,
                originalPrice = variant.pricing.sellingPrice.takeIf { variant.pricing.hasDiscount },
                large = true,
            )
        }
        if (variant.pricing.hasDiscount) {
            Text(
                variant.pricing.discountPercentLabel,
                style = PLFont.button,
                color = PLColor.backgroundPrimary,
                modifier = Modifier
                    .background(PLColor.goldBright, RoundedCornerShape(PLRadius.badge))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun VariantSelectors(state: ProductDetailsUiState, actions: ProductDetailsActions) {
    Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.lg)) {
        if (state.hasMultipleColors) {
            Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
                SectionLabel(stringResource(R.string.details_section_color), state.selectedColor?.let { stringResource(it.labelRes) })
                PLColorSelector(
                    options = state.availableColors.map {
                        PLColorOption(it, stringResource(it.labelRes), Color(it.swatchArgb), it.needsSwatchOutline)
                    },
                    enabled = state.enabledColors,
                    selected = state.selectedColor,
                    onSelect = actions.onColorSelected,
                )
            }
        }
        if (state.hasMultipleSizes) {
            Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
                SectionLabel(stringResource(R.string.details_section_size), state.selectedSize)
                PLSizeSelector(
                    sizes = state.availableSizes,
                    enabled = state.enabledSizes,
                    selected = state.selectedSize,
                    onSelect = actions.onSizeSelected,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, value: String?) {
    Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = PLFont.label.copy(letterSpacing = 1.5.sp), color = PLColor.textMuted)
        if (value != null) {
            Text("·", color = PLColor.textDisabled)
            Text(value, style = PLFont.caption, color = PLColor.textSecondary)
        }
    }
}

@Composable
private fun DescriptionAndStock(variant: ProductVariant) {
    val shape = RoundedCornerShape(PLRadius.card)
    Column(
        Modifier
            .fillMaxWidth()
            .background(PLColor.backgroundElevated, shape)
            .border(1.dp, PLColor.borderSubtle, shape)
            .padding(PLSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
    ) {
        SectionLabel(stringResource(R.string.details_section_about), null)
        variant.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = PLFont.body, color = PLColor.textSecondary)
        }
        val (dotColor, stockText) = when (variant.status) {
            ProductStatus.ACTIVE -> PLColor.success to stringResource(R.string.details_stock_available, variant.stockQuantity)
            ProductStatus.OUT_OF_STOCK -> PLColor.error to stringResource(R.string.product_status_out_of_stock)
            ProductStatus.INACTIVE, ProductStatus.UNKNOWN -> PLColor.textDisabled to stringResource(R.string.product_status_unavailable)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape),
            )
            Text(stockText, style = PLFont.caption, color = PLColor.textPrimary)
        }
    }
}

@Composable
private fun DetailsList(variant: ProductVariant) {
    Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
        SectionLabel(stringResource(R.string.details_section_details), null)
        Column {
            DetailRow(stringResource(R.string.details_row_category), stringResource(variant.category.labelRes))
            HorizontalDivider(color = PLColor.borderSubtle)
            DetailRow(stringResource(R.string.details_row_dimensions), variant.sizeLabel)
            variant.barcode?.let {
                HorizontalDivider(color = PLColor.borderSubtle)
                DetailRow(stringResource(R.string.details_row_barcode), it)
            }
            HorizontalDivider(color = PLColor.borderSubtle)
            DetailRow(stringResource(R.string.details_row_sku), variant.slug)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.padding(vertical = PLSpacing.sm)) {
        Text(label, style = PLFont.caption, color = PLColor.textMuted, modifier = Modifier.weight(1f))
        Text(value, style = PLFont.caption, color = PLColor.textSecondary)
    }
}

@Composable
private fun FooterBar(state: ProductDetailsUiState, actions: ProductDetailsActions) {
    Column(Modifier.background(PLColor.backgroundSecondary)) {
        HorizontalDivider(color = PLColor.borderSubtle)
        Row(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = PLSpacing.xl, vertical = PLSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PLQuantityStepper(
                value = state.quantity,
                onValueChange = actions.onQuantityChange,
                range = 1..state.maxQuantity.coerceAtLeast(1),
                enabled = state.canPurchase,
                modifier = Modifier.alpha(if (state.canPurchase) 1f else 0.4f),
            )
            PLButton(
                text = stringResource(if (state.canPurchase) R.string.details_buy else R.string.details_unavailable_button),
                onClick = actions.onBuy,
                enabled = state.canPurchase,
                modifier = Modifier.weight(1f),
            )
            val shape = RoundedCornerShape(PLRadius.button)
            IconButton(
                onClick = actions.onAddToCart,
                enabled = state.canPurchase,
                modifier = Modifier
                    .size(52.dp)
                    .background(PLColor.backgroundElevated, shape)
                    .border(1.dp, PLColor.borderSubtle, shape),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = PLColor.goldBright,
                    disabledContentColor = PLColor.textDisabled,
                ),
            ) {
                Icon(Icons.Outlined.AddShoppingCart, contentDescription = stringResource(R.string.details_add_to_cart))
            }
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(PLColor.backgroundPrimary.copy(alpha = 0.6f), CircleShape),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = PLColor.textPrimary)
    }
}

@Composable
private fun DetailsError(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(PLSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = PLColor.warning, modifier = Modifier.size(36.dp))
        Text(title, style = PLFont.h2, color = PLColor.textPrimary, textAlign = TextAlign.Center)
        Text(message, style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
        PLButton(action, onClick = onAction, modifier = Modifier.widthIn(max = 220.dp))
        Spacer(Modifier.height(PLSpacing.xl))
    }
}

private fun previewVariant(id: String, color: ProductColor, width: Double, stock: Int) = ProductVariant(
    id = id,
    name = "Luna",
    slug = "luna-$id",
    description = "Placa de nome em acrílico espelhado, cortada a laser.",
    stockQuantity = stock,
    category = ProductCategory.NAMES,
    barcode = "7890000000001",
    status = if (stock > 0) ProductStatus.ACTIVE else ProductStatus.OUT_OF_STOCK,
    pricing = ProductPricing(BigDecimal("50"), BigDecimal("0.15"), BigDecimal("42.50")),
    specifications = ProductVariantSpecifications(color, ProductDimensions(width = width, height = width, unit = "cm")),
)

@Preview
@Composable
private fun ProductDetailsPreview() {
    val variants = listOf(
        previewVariant("1", ProductColor.GOLD, 10.0, 5),
        previewVariant("2", ProductColor.SILVER, 10.0, 0),
        previewVariant("3", ProductColor.GOLD, 20.0, 2),
    )
    PaintresLumiereTheme {
        ProductDetailsScreen(
            ProductDetailsUiState(DetailsLoadState.Loaded, variants, ProductColor.GOLD, "10 × 10cm", 1),
            ProductDetailsActions(),
        )
    }
}

@Preview
@Composable
private fun ProductDetailsErrorPreview() {
    PaintresLumiereTheme {
        ProductDetailsScreen(
            ProductDetailsUiState(DetailsLoadState.Failed(UiText.Raw("Sem conexão."))),
            ProductDetailsActions(),
        )
    }
}
