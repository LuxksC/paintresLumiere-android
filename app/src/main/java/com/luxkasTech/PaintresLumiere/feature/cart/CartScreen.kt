package com.luxkasTech.PaintresLumiere.feature.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLProductPriceRow
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLQuantityStepper
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLRemoteImage
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.data.cart.Cart
import com.luxkasTech.PaintresLumiere.data.cart.CartItem
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus
import java.math.BigDecimal

@Composable
fun CartRoute(
    onBack: () -> Unit,
    onBrowseCatalog: () -> Unit,
    onCheckoutRequested: () -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CartScreen(
        state = state,
        actions = CartActions(
            onBack = onBack,
            onBrowseCatalog = onBrowseCatalog,
            onQuantityChange = viewModel::changeQuantity,
            onRemove = viewModel::removeItem,
            onClear = viewModel::requestClear,
            onConfirmClear = viewModel::confirmClear,
            onDismissClear = viewModel::dismissClear,
            onCheckout = onCheckoutRequested,
        ),
    )
}

data class CartActions(
    val onBack: () -> Unit = {},
    val onBrowseCatalog: () -> Unit = {},
    val onQuantityChange: (itemId: String, quantity: Int) -> Unit = { _, _ -> },
    val onRemove: (itemId: String) -> Unit = {},
    val onClear: () -> Unit = {},
    val onConfirmClear: () -> Unit = {},
    val onDismissClear: () -> Unit = {},
    val onCheckout: () -> Unit = {},
)

@Composable
fun CartScreen(state: CartUiState, actions: CartActions) {
    val cart = state.cart
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding(),
    ) {
        CartTopBar(showClear = !cart.isEmpty, onBack = actions.onBack, onClear = actions.onClear)
        if (cart.isEmpty) {
            EmptyCart(actions.onBrowseCatalog)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = PLSpacing.xl, vertical = PLSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
            ) {
                items(cart.items, key = { it.id }) { item ->
                    CartItemRow(
                        item = item,
                        onQuantityChange = { actions.onQuantityChange(item.id, it) },
                        onRemove = { actions.onRemove(item.id) },
                    )
                }
            }
            CartSummary(cart, actions.onCheckout)
        }
    }

    if (state.confirmingClear) {
        AlertDialog(
            onDismissRequest = actions.onDismissClear,
            title = { Text(stringResource(R.string.cart_clear_title), style = PLFont.h2) },
            confirmButton = {
                TextButton(onClick = actions.onConfirmClear) {
                    Text(stringResource(R.string.cart_clear_confirm), color = PLColor.error, style = PLFont.button)
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissClear) {
                    Text(stringResource(R.string.common_cancel), color = PLColor.textPrimary, style = PLFont.button)
                }
            },
            containerColor = PLColor.backgroundElevated,
            titleContentColor = PLColor.textPrimary,
        )
    }
}

@Composable
private fun CartTopBar(showClear: Boolean, onBack: () -> Unit, onClear: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = PLSpacing.sm, vertical = PLSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = PLColor.textPrimary)
        }
        Text(
            stringResource(R.string.cart_title),
            style = PLFont.h2,
            color = PLColor.textPrimary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (showClear) {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.cart_clear), style = PLFont.link, color = PLColor.textMuted)
            }
        }
    }
    HorizontalDivider(color = PLColor.borderSubtle)
}

@Composable
private fun CartItemRow(item: CartItem, onQuantityChange: (Int) -> Unit, onRemove: () -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Row(
        Modifier
            .fillMaxWidth()
            .background(PLColor.backgroundElevated, shape)
            .border(1.dp, if (item.isUnavailable || item.exceedsStock) PLColor.warning else PLColor.borderSubtle, shape)
            .padding(PLSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
    ) {
        PLRemoteImage(
            url = item.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(PLRadius.card - 2.dp)),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    item.name,
                    style = PLFont.button,
                    color = PLColor.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, stringResource(R.string.cart_remove_item, item.name), tint = PLColor.textMuted)
                }
            }
            Text(
                "${stringResource(item.color.labelRes)} · ${item.sizeLabel}",
                style = PLFont.caption,
                color = PLColor.textMuted,
            )
            PLProductPriceRow(finalPrice = item.finalUnitPrice, originalPrice = item.unitPrice.takeIf { item.hasDiscount })
            when {
                item.isUnavailable -> Flag(stringResource(R.string.cart_flag_unavailable))
                item.exceedsStock -> Flag(stringResource(R.string.cart_flag_stock, item.stockQuantity))
            }
            PLQuantityStepper(
                value = item.quantity,
                onValueChange = onQuantityChange,
                range = 1..item.stockQuantity.coerceAtLeast(1),
                enabled = !item.isUnavailable,
            )
        }
    }
}

@Composable
private fun Flag(text: String) {
    Text(text, style = PLFont.caption, color = PLColor.warning)
}

@Composable
private fun CartSummary(cart: Cart, onCheckout: () -> Unit) {
    Column(Modifier.background(PLColor.backgroundSecondary)) {
        HorizontalDivider(color = PLColor.borderSubtle)
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = PLSpacing.xl, vertical = PLSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.xs),
        ) {
            SummaryRow(stringResource(R.string.cart_subtotal), cart.subtotal.brlFormatted(), PLColor.textSecondary)
            if (cart.discountTotal.signum() > 0) {
                SummaryRow(stringResource(R.string.cart_discount), "−" + cart.discountTotal.brlFormatted(), PLColor.success)
            }
            Row(Modifier.padding(top = PLSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.cart_total), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.weight(1f))
                Text(cart.total.brlFormatted(), style = PLFont.h2, color = PLColor.goldBright)
            }
            PLButton(
                text = stringResource(R.string.cart_checkout),
                onClick = onCheckout,
                enabled = cart.canCheckout,
                modifier = Modifier.padding(top = PLSpacing.sm),
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color) {
    Row {
        Text(label, style = PLFont.body, color = PLColor.textMuted, modifier = Modifier.weight(1f))
        Text(value, style = PLFont.body, color = valueColor)
    }
}

@Composable
private fun EmptyCart(onBrowseCatalog: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(PLSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
        ) {
            Icon(Icons.Outlined.RemoveShoppingCart, contentDescription = null, tint = PLColor.goldAntique, modifier = Modifier.size(40.dp))
            Text(stringResource(R.string.cart_empty_title), style = PLFont.h2, color = PLColor.textPrimary)
            Text(
                stringResource(R.string.cart_empty_message),
                style = PLFont.body,
                color = PLColor.textMuted,
                textAlign = TextAlign.Center,
            )
            PLButton(stringResource(R.string.cart_browse), onClick = onBrowseCatalog, modifier = Modifier.widthIn(max = 240.dp))
        }
    }
}

/** Cart icon with an item-count badge, for top bars. */
@Composable
fun CartIconWithBadge(count: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box {
            Icon(
                Icons.Outlined.ShoppingCart,
                contentDescription = if (count > 0) {
                    stringResource(R.string.cart_icon_a11y_count, count)
                } else {
                    stringResource(R.string.home_cart)
                },
                tint = PLColor.textMuted,
            )
            if (count > 0) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = PLFont.label,
                    color = PLColor.backgroundPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-6).dp)
                        .background(PLColor.goldBright, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun CartScreenPreview() {
    val items = listOf(
        CartItem("1", "v1", "Luna", "luna", ProductColor.GOLD, "10 × 10cm", null, BigDecimal("50"), BigDecimal("42.50"), 5, ProductStatus.ACTIVE, 2),
        CartItem("2", "v2", "Sol", "sol", ProductColor.SILVER, "20 × 20cm", null, BigDecimal("19.90"), BigDecimal("19.90"), 1, ProductStatus.ACTIVE, 3),
    )
    PaintresLumiereTheme {
        CartScreen(CartUiState(Cart(items)), CartActions())
    }
}

@Preview
@Composable
private fun EmptyCartPreview() {
    PaintresLumiereTheme {
        CartScreen(CartUiState(), CartActions())
    }
}
