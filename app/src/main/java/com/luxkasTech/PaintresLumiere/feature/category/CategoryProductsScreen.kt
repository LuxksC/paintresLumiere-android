package com.luxkasTech.PaintresLumiere.feature.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLProductCard
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.feature.product.toCardData

@Composable
fun CategoryProductsRoute(
    onBack: () -> Unit,
    onProductSelected: (sku: String) -> Unit,
    viewModel: CategoryProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategoryProductsScreen(state, onBack, onProductSelected, viewModel::refresh, viewModel::retry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    state: CategoryProductsUiState,
    onBack: () -> Unit,
    onProductSelected: (sku: String) -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding(),
    ) {
        Row(Modifier.padding(horizontal = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = PLColor.textPrimary)
            }
            Text(
                stringResource(state.category.labelRes),
                style = PLFont.h2,
                color = PLColor.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PLColor.goldMid)
            }
            state.error != null && state.products.isEmpty() -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(PLSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.error.asString(), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
                PLButton(stringResource(R.string.common_try_again), onClick = onRetry, modifier = Modifier.widthIn(max = 220.dp))
            }
            state.products.isEmpty() -> Box(Modifier.fillMaxSize().padding(PLSpacing.xl), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.category_empty), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
            }
            else -> PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = onRefresh, modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(PLSpacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.products, key = { it.id }) { product ->
                        PLProductCard(product.toCardData(), onClick = { onProductSelected(product.sku) })
                    }
                }
            }
        }
    }
}
