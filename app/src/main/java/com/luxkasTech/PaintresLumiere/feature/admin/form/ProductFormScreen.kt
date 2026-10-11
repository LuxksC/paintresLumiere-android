package com.luxkasTech.PaintresLumiere.feature.admin.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.data.product.ProductCategory
import com.luxkasTech.PaintresLumiere.data.product.ProductColor
import com.luxkasTech.PaintresLumiere.data.product.ProductStatus

@Composable
fun ProductFormRoute(
    onFinished: () -> Unit,
    viewModel: ProductFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { if (it == ProductFormViewModel.Event.Saved) onFinished() }
    }
    ProductFormScreen(state, viewModel::update, viewModel::save, viewModel::retry, onFinished)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductFormScreen(
    state: ProductFormUiState,
    onUpdate: ((ProductFormUiState) -> ProductFormUiState) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Row(Modifier.padding(horizontal = PLSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), tint = PLColor.textPrimary)
            }
            Text(
                stringResource(
                    when (state.mode) {
                        ProductFormUiState.Mode.Create -> R.string.admin_form_title_create
                        ProductFormUiState.Mode.AddVariant -> R.string.admin_form_title_variant
                        ProductFormUiState.Mode.Edit -> R.string.admin_form_title_edit
                    },
                ),
                style = PLFont.h2,
                color = PLColor.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PLColor.goldMid)
            }
            state.loadError != null -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(PLSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(PLSpacing.md, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.loadError.asString(), style = PLFont.body, color = PLColor.textMuted, textAlign = TextAlign.Center)
                PLButton(stringResource(R.string.common_try_again), onClick = onRetry, modifier = Modifier.widthIn(max = 220.dp))
            }
            else -> FormFields(state, onUpdate, onSave)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormFields(
    state: ProductFormUiState,
    onUpdate: ((ProductFormUiState) -> ProductFormUiState) -> Unit,
    onSave: () -> Unit,
) {
    val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PLSpacing.xl, vertical = PLSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
    ) {
        PLInputField(
            label = stringResource(R.string.admin_form_sku),
            value = state.sku,
            onValueChange = { v -> onUpdate { it.copy(sku = v) } },
            errorMessage = state.errors[FormField.Sku]?.asString(),
        )
        PLInputField(
            label = stringResource(R.string.admin_form_name),
            value = state.name,
            onValueChange = { v -> onUpdate { it.copy(name = v) } },
            errorMessage = state.errors[FormField.Name]?.asString(),
        )
        PLInputField(
            label = stringResource(R.string.admin_form_description),
            value = state.description,
            onValueChange = { v -> onUpdate { it.copy(description = v) } },
        )

        ChipGroup(stringResource(R.string.admin_form_category), state.errors[FormField.Category]?.asString()) {
            ProductCategory.entries.filter { it != ProductCategory.UNKNOWN }.forEach { category ->
                BrandChip(stringResource(category.labelRes), state.category == category) {
                    onUpdate { it.copy(category = category) }
                }
            }
        }
        ChipGroup(stringResource(R.string.admin_form_color), state.errors[FormField.Color]?.asString()) {
            ProductColor.selectable.forEach { color ->
                BrandChip(stringResource(color.labelRes), state.color == color) { onUpdate { it.copy(color = color) } }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.md)) {
            PLInputField(
                label = stringResource(R.string.admin_form_price),
                value = state.price,
                onValueChange = { v -> onUpdate { it.copy(price = v) } },
                placeholder = "0,00",
                errorMessage = state.errors[FormField.Price]?.asString(),
                keyboardOptions = decimal,
                modifier = Modifier.weight(1f),
            )
            PLInputField(
                label = stringResource(R.string.admin_form_discount),
                value = state.discountPercent,
                onValueChange = { v -> onUpdate { it.copy(discountPercent = v) } },
                placeholder = "0",
                errorMessage = state.errors[FormField.Discount]?.asString(),
                keyboardOptions = decimal,
                modifier = Modifier.weight(1f),
            )
        }
        PLInputField(
            label = stringResource(R.string.admin_form_stock),
            value = state.stock,
            onValueChange = { v -> onUpdate { it.copy(stock = v.filter(Char::isDigit)) } },
            errorMessage = state.errors[FormField.Stock]?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        ChipGroup(stringResource(R.string.admin_form_status), null) {
            listOf(ProductStatus.ACTIVE, ProductStatus.OUT_OF_STOCK, ProductStatus.INACTIVE).forEach { status ->
                BrandChip(stringResource(status.labelRes), state.status == status) { onUpdate { it.copy(status = status) } }
            }
        }

        Text(
            stringResource(R.string.admin_form_dimensions),
            style = PLFont.label.copy(letterSpacing = 1.sp),
            color = PLColor.textMuted,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
            PLInputField(stringResource(R.string.admin_form_width), state.width, { v -> onUpdate { it.copy(width = v) } }, Modifier.weight(1f), keyboardOptions = decimal)
            PLInputField(stringResource(R.string.admin_form_height), state.height, { v -> onUpdate { it.copy(height = v) } }, Modifier.weight(1f), keyboardOptions = decimal)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
            PLInputField(stringResource(R.string.admin_form_length), state.length, { v -> onUpdate { it.copy(length = v) } }, Modifier.weight(1f), keyboardOptions = decimal)
            PLInputField(stringResource(R.string.admin_form_thickness), state.thickness, { v -> onUpdate { it.copy(thickness = v) } }, Modifier.weight(1f), keyboardOptions = decimal)
            PLInputField(stringResource(R.string.admin_form_unit), state.unit, { v -> onUpdate { it.copy(unit = v) } }, Modifier.weight(1f))
        }
        PLInputField(
            label = stringResource(R.string.admin_form_barcode),
            value = state.barcode,
            onValueChange = { v -> onUpdate { it.copy(barcode = v.filter(Char::isDigit).take(14)) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        PLButton(
            text = stringResource(R.string.admin_form_save),
            onClick = onSave,
            type = if (state.isSaving) PLButtonType.Loading else PLButtonType.Primary,
            modifier = Modifier.padding(top = PLSpacing.sm),
        )
        Spacer(Modifier.height(PLSpacing.xl))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(label: String, error: String?, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
        Text(label.uppercase(), style = PLFont.label.copy(letterSpacing = 1.sp), color = if (error != null) PLColor.error else PLColor.textMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(PLSpacing.sm)) { content() }
        if (error != null) Text(error, style = PLFont.caption, color = PLColor.error)
    }
}

@Composable
private fun BrandChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = PLFont.caption) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = PLColor.backgroundElevated,
            labelColor = PLColor.textPrimary,
            selectedContainerColor = PLColor.goldMid,
            selectedLabelColor = PLColor.backgroundPrimary,
        ),
    )
}

@Preview
@Composable
private fun ProductFormPreview() {
    PaintresLumiereTheme {
        ProductFormScreen(
            ProductFormUiState(name = "Luna", sku = "LUNA", category = ProductCategory.NAMES, color = ProductColor.GOLD, price = "50,00"),
            {}, {}, {}, {},
        )
    }
}
