package com.luxkasTech.PaintresLumiere.feature.admin.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLRemoteImage
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.data.product.Product
import com.luxkasTech.PaintresLumiere.data.product.ProductPricing
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import java.math.BigDecimal

@Composable
fun AdminProductsRoute(
    onBack: () -> Unit,
    onProductSelected: (sku: String) -> Unit,
    onCreateProduct: () -> Unit,
    viewModel: AdminProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AdminProductsScreen(state, onBack, viewModel::onQueryChange, viewModel::load, onProductSelected, onCreateProduct)
}

@Composable
fun AdminProductsScreen(
    state: AdminProductsUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onProductSelected: (sku: String) -> Unit,
    onCreateProduct: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.padding(horizontal = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = PLColor.textPrimary)
                }
                Text(
                    stringResource(R.string.admin_products_title),
                    style = PLFont.h2,
                    color = PLColor.textPrimary,
                    modifier = Modifier.semantics { heading() },
                )
            }
            PLInputField(
                label = stringResource(R.string.admin_products_search),
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.admin_products_search_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.padding(horizontal = PLSpacing.xl, vertical = PLSpacing.sm),
            )
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PLColor.goldMid)
                }
                state.error != null -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(PLSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.error.asString(), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
                    PLButton(stringResource(R.string.common_try_again), onClick = onRetry, modifier = Modifier.widthIn(max = 220.dp))
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = PLSpacing.xl, end = PLSpacing.xl, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
                ) {
                    item(key = "notice") { ActiveOnlyNotice() }
                    if (state.visibleProducts.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                stringResource(R.string.admin_products_empty),
                                style = PLFont.body,
                                color = PLColor.textMuted,
                                modifier = Modifier.padding(vertical = PLSpacing.xl),
                            )
                        }
                    }
                    items(state.visibleProducts, key = { it.id }) { product ->
                        AdminProductRow(product) { onProductSelected(product.sku) }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onCreateProduct,
            containerColor = PLColor.goldMid,
            contentColor = PLColor.backgroundPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(PLSpacing.lg),
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.admin_products_create))
        }
    }
}

@Composable
private fun ActiveOnlyNotice() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PLColor.backgroundElevated, RoundedCornerShape(PLRadius.card))
            .padding(PLSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm),
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = PLColor.textMuted, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.admin_products_active_only), style = PLFont.caption, color = PLColor.textMuted)
    }
}

@Composable
private fun AdminProductRow(product: Product, onClick: () -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(1.dp, PLColor.borderSubtle, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(PLSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PLRemoteImage(
            url = product.primaryImageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(PLRadius.card - 4.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(product.name, style = PLFont.button, color = PLColor.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(R.string.admin_products_row_meta, product.sku, product.stockQuantity),
                style = PLFont.caption,
                color = PLColor.textMuted,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(product.pricing.finalPrice.brlFormatted(), style = PLFont.button, color = PLColor.goldBright)
            Text(
                stringResource(product.status.labelRes),
                style = PLFont.caption,
                color = if (product.status.isPurchasable) PLColor.success else PLColor.warning,
            )
        }
    }
}

@Preview
@Composable
private fun AdminProductsPreview() {
    val products = listOf(
        Product("1", "LUNA", "Luna", "luna", stockQuantity = 5, status = ProductStatus.ACTIVE, pricing = ProductPricing(BigDecimal("50"), BigDecimal.ZERO, BigDecimal("50"))),
        Product("2", "SOL", "Sol", "sol", stockQuantity = 0, status = ProductStatus.OUT_OF_STOCK, pricing = ProductPricing(BigDecimal("20"), BigDecimal.ZERO, BigDecimal("20"))),
    )
    PaintresLumiereTheme {
        AdminProductsScreen(AdminProductsUiState(products, isLoading = false), {}, {}, {}, {}, {})
    }
}
