package com.luxkasTech.PaintresLumiere.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
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
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLCategoryCard
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLProductCard
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.data.product.Product
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.feature.product.icon
import com.luxkasTech.PaintresLumiere.data.product.ProductPricing
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import com.luxkasTech.PaintresLumiere.feature.cart.CartIconWithBadge
import com.luxkasTech.PaintresLumiere.feature.product.toCardData
import java.math.BigDecimal

@Composable
fun HomeRoute(
    onProductSelected: (sku: String) -> Unit,
    onCartRequested: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onProductClick = { onProductSelected(it.sku) },
        onRefresh = viewModel::refresh,
        onRetry = viewModel::retry,
        onNotificationsClick = viewModel::onNotificationsClick,
        onCartClick = onCartRequested,
        onCategoryClick = viewModel::onCategoryClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onProductClick: (Product) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onCategoryClick: (ProductCategory) -> Unit = {},
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding(),
    ) {
        when {
            state.isLoading -> HomeLoading()
            state.showsFullScreenError -> HomeError(state.error, onRetry)
            else -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                CatalogGrid(state, onProductClick, onNotificationsClick, onCartClick, onCategoryClick)
            }
        }
    }
}

@Composable
private fun CatalogGrid(
    state: HomeUiState,
    onProductClick: (Product) -> Unit,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onCategoryClick: (ProductCategory) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = PLSpacing.xl, end = PLSpacing.xl, bottom = PLSpacing.xxl),
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "header") {
            Column {
                HomeHeader(state.cartCount, onNotificationsClick, onCartClick)
                PLDivider(Modifier.padding(top = PLSpacing.md))
            }
        }
        if (state.popular.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "popular") {
                PopularSection(state.popular, onProductClick)
            }
        }
        if (state.categories.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "categoriesHeader") {
                SectionLabel(stringResource(R.string.home_section_categories), Modifier.padding(top = PLSpacing.sm))
            }
            items(state.categories, key = { "category-${it.category.raw}" }) { summary ->
                PLCategoryCard(
                    label = stringResource(summary.category.labelRes),
                    countLabel = pluralStringResource(R.plurals.home_pieces_count, summary.productCount, summary.productCount),
                    imageUrl = summary.imageUrl,
                    icon = summary.category.icon,
                    onClick = { onCategoryClick(summary.category) },
                )
            }
        } else if (state.catalog.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                HomeEmpty()
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }, key = "collectionHeader") {
                Row(Modifier.padding(top = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    SectionLabel(stringResource(R.string.home_section_collection), Modifier.weight(1f))
                    Text(
                        pluralStringResource(R.plurals.home_pieces_count, state.catalog.size, state.catalog.size),
                        style = PLFont.caption,
                        color = PLColor.textDisabled,
                    )
                }
            }
            items(state.catalog, key = { it.id }) { product ->
                PLProductCard(data = product.toCardData(), onClick = { onProductClick(product) })
            }
        }
    }
}

@Composable
private fun HomeHeader(cartCount: Int, onNotificationsClick: () -> Unit, onCartClick: () -> Unit) {
    Row(Modifier.padding(top = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.app_name),
                style = PLFont.h2,
                color = PLColor.goldBright,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(R.string.home_header_subtitle),
                style = PLFont.label.copy(letterSpacing = 2.sp),
                color = PLColor.textMuted,
            )
        }
        IconButton(onClick = onNotificationsClick) {
            Icon(Icons.Outlined.Notifications, stringResource(R.string.home_notifications), tint = PLColor.textMuted)
        }
        CartIconWithBadge(count = cartCount, onClick = onCartClick)
    }
}

@Composable
private fun PopularSection(products: List<Product>, onProductClick: (Product) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
        SectionLabel(stringResource(R.string.home_section_popular), Modifier.padding(top = PLSpacing.md))
        // The grid has horizontal content padding; let the carousel bleed to the screen edges.
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
            contentPadding = PaddingValues(horizontal = PLSpacing.xl),
            modifier = Modifier
                .fillMaxWidth()
                .layoutBleed(PLSpacing.xl),
        ) {
            items(products, key = { it.id }) { product ->
                PLProductCard(
                    data = product.toCardData(),
                    onClick = { onProductClick(product) },
                    modifier = Modifier.width(180.dp),
                )
            }
        }
        Spacer(Modifier.height(PLSpacing.md))
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = PLFont.label.copy(letterSpacing = 1.sp),
        color = PLColor.textMuted,
        modifier = modifier.semantics { heading() },
    )
}

@Composable
private fun HomeLoading() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(color = PLColor.goldMid)
        Text(stringResource(R.string.home_loading), style = PLFont.body, color = PLColor.textMuted)
    }
}

@Composable
private fun HomeError(error: UiText?, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(PLSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = PLColor.warning, modifier = Modifier.size(36.dp))
        Text(stringResource(R.string.home_error_title), style = PLFont.h2, color = PLColor.textPrimary, textAlign = TextAlign.Center)
        error?.let { Text(it.asString(), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center) }
        PLButton(stringResource(R.string.common_try_again), onClick = onRetry, modifier = Modifier.widthIn(max = 220.dp))
    }
}

@Composable
private fun HomeEmpty() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = PLSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.Inbox, contentDescription = null, tint = PLColor.goldAntique, modifier = Modifier.size(36.dp))
        Text(stringResource(R.string.home_empty_title), style = PLFont.h2, color = PLColor.textPrimary)
        Text(
            stringResource(R.string.home_empty_message),
            style = PLFont.body,
            color = PLColor.textMuted,
            textAlign = TextAlign.Center,
        )
    }
}

private fun previewProduct(id: String, name: String, discount: Boolean = false, status: ProductStatus = ProductStatus.ACTIVE) =
    Product(
        id = id,
        sku = "SKU-$id",
        name = name,
        slug = name.lowercase(),
        status = status,
        pricing = if (discount) {
            ProductPricing(BigDecimal("50"), BigDecimal("0.15"), BigDecimal("42.50"))
        } else {
            ProductPricing(BigDecimal("19.90"), BigDecimal.ZERO, BigDecimal("19.90"))
        },
    )

@Preview
@Composable
private fun HomeScreenPreview() {
    val products = listOf(
        previewProduct("1", "Luna", discount = true),
        previewProduct("2", "Sol"),
        previewProduct("3", "Estrela", status = ProductStatus.OUT_OF_STOCK),
        previewProduct("4", "Nome personalizado"),
    )
    PaintresLumiereTheme {
        HomeScreen(HomeUiState(catalog = products, popular = products.take(2), isLoading = false, cartCount = 3), {}, {}, {}, {}, {})
    }
}
