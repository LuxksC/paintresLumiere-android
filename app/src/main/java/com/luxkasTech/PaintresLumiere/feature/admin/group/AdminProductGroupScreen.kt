package com.luxkasTech.PaintresLumiere.feature.admin.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLRemoteImage
import com.luxkasTech.PaintresLumiere.core.designsystem.format.brlFormatted
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.data.product.ProductVariant

@Composable
fun AdminProductGroupRoute(
    onBack: () -> Unit,
    onEditVariant: (sku: String, variantId: String) -> Unit,
    onAddVariant: (sku: String) -> Unit,
    viewModel: AdminProductGroupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pickingFor by rememberSaveable { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val variantId = pickingFor
        pickingFor = null
        if (uri == null || variantId == null) return@rememberLauncherForActivityResult
        scope.launch {
            val picked = withContext(Dispatchers.IO) {
                runCatching {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    bytes?.let { it to mime }
                }.getOrNull()
            }
            picked?.let { (bytes, mime) -> viewModel.uploadImage(variantId, bytes, mime) }
        }
    }
    AdminProductGroupScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
        onEdit = { onEditVariant(state.sku, it.id) },
        onDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onDismissDelete = viewModel::dismissDelete,
        onAddVariant = { onAddVariant(state.sku) },
        onAdjustStock = viewModel::adjustStock,
        onAddImage = { variant ->
            pickingFor = variant.id
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
    )
}

@Composable
fun AdminProductGroupScreen(
    state: AdminProductGroupUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEdit: (ProductVariant) -> Unit,
    onDelete: (ProductVariant) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onAddVariant: () -> Unit,
    onAdjustStock: (ProductVariant, Int) -> Unit = { _, _ -> },
    onAddImage: (ProductVariant) -> Unit = {},
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
                Column {
                    Text(
                        state.variants.firstOrNull()?.name ?: state.sku,
                        style = PLFont.h2,
                        color = PLColor.textPrimary,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(stringResource(R.string.admin_group_subtitle, state.sku), style = PLFont.caption, color = PLColor.textMuted)
                }
            }
            when {
                state.isLoading && state.variants.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    contentPadding = PaddingValues(start = PLSpacing.xl, end = PLSpacing.xl, top = PLSpacing.md, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
                ) {
                    items(state.variants, key = { it.id }) { variant ->
                        VariantRow(
                            variant = variant,
                            isUploading = state.uploadingVariantId == variant.id,
                            onEdit = { onEdit(variant) },
                            onDelete = { onDelete(variant) },
                            onAdjustStock = { delta -> onAdjustStock(variant, delta) },
                            onAddImage = { onAddImage(variant) },
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onAddVariant,
            containerColor = PLColor.goldMid,
            contentColor = PLColor.backgroundPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(PLSpacing.lg),
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.admin_group_add_variant))
        }
    }

    state.pendingDelete?.let { variant ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text(stringResource(R.string.admin_group_delete_title), style = PLFont.h2) },
            text = {
                Text(
                    stringResource(R.string.admin_group_delete_message, variant.name, stringResource(variant.color.labelRes), variant.sizeLabel),
                    style = PLFont.body,
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.profile_delete_confirm), color = PLColor.error, style = PLFont.button)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(R.string.common_cancel), color = PLColor.textPrimary, style = PLFont.button)
                }
            },
            containerColor = PLColor.backgroundElevated,
            titleContentColor = PLColor.textPrimary,
            textContentColor = PLColor.textMuted,
        )
    }
}

@Composable
private fun VariantRow(
    variant: ProductVariant,
    isUploading: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: (Int) -> Unit,
    onAddImage: () -> Unit,
) {
    val shape = RoundedCornerShape(PLRadius.card)
    Column(
        Modifier
            .fillMaxWidth()
            .background(PLColor.backgroundElevated, shape)
            .border(1.dp, PLColor.borderSubtle, shape)
            .padding(PLSpacing.sm),
    ) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PLRemoteImage(
            url = variant.images.firstOrNull(),
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(PLRadius.card - 4.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text("${stringResource(variant.color.labelRes)} · ${variant.sizeLabel}", style = PLFont.button, color = PLColor.textPrimary)
            Text(
                stringResource(R.string.admin_group_row_meta, variant.pricing.finalPrice.brlFormatted(), variant.stockQuantity, stringResource(variant.status.labelRes)),
                style = PLFont.caption,
                color = PLColor.textMuted,
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, stringResource(R.string.admin_group_edit), tint = PLColor.goldMid)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, stringResource(R.string.admin_group_delete), tint = PLColor.error)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = PLSpacing.xs)) {
        Text(stringResource(R.string.admin_stock_label), style = PLFont.caption, color = PLColor.textMuted)
        IconButton(onClick = { onAdjustStock(-1) }, enabled = variant.stockQuantity > 0) {
            Icon(Icons.Filled.Remove, stringResource(R.string.ds_stepper_decrease), tint = PLColor.textPrimary)
        }
        Text(variant.stockQuantity.toString(), style = PLFont.button, color = PLColor.textPrimary)
        IconButton(onClick = { onAdjustStock(1) }) {
            Icon(Icons.Filled.Add, stringResource(R.string.ds_stepper_increase), tint = PLColor.textPrimary)
        }
        Spacer(Modifier.weight(1f))
        if (isUploading) {
            CircularProgressIndicator(color = PLColor.goldMid, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            TextButton(onClick = onAddImage) {
                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = PLColor.goldMid, modifier = Modifier.size(18.dp))
                Text(" " + stringResource(R.string.admin_image_add), style = PLFont.link, color = PLColor.goldMid)
            }
        }
    }
    }
}
