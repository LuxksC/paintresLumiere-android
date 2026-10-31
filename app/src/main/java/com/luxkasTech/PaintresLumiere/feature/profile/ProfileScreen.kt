package com.luxkasTech.PaintresLumiere.feature.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLDivider
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile

@Composable
fun ProfileRoute(viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        actions = ProfileActions(
            onMenuItemClick = viewModel::onMenuItemClick,
            onLogout = viewModel::logout,
            onDeleteAccount = viewModel::requestDeleteAccount,
            onConfirmDelete = viewModel::confirmDeleteAccount,
            onDismissDelete = viewModel::dismissDeleteAccount,
            onRetryProfile = viewModel::loadProfile,
        ),
    )
}

data class ProfileActions(
    val onMenuItemClick: (ProfileMenuItem) -> Unit = {},
    val onLogout: () -> Unit = {},
    val onDeleteAccount: () -> Unit = {},
    val onConfirmDelete: () -> Unit = {},
    val onDismissDelete: () -> Unit = {},
    val onRetryProfile: () -> Unit = {},
)

@Composable
fun ProfileScreen(state: ProfileUiState, actions: ProfileActions) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PLColor.backgroundPrimary)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PLSpacing.xl),
    ) {
        Column(Modifier.padding(top = PLSpacing.sm)) {
            Text(
                stringResource(R.string.profile_title),
                style = PLFont.h1,
                color = PLColor.textPrimary,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                stringResource(R.string.profile_subtitle),
                style = PLFont.label.copy(letterSpacing = 1.5.sp),
                color = PLColor.textMuted,
            )
        }
        PLDivider(Modifier.padding(vertical = PLSpacing.lg))

        ProfileIdentity(state.profile, state.isProfileLoading, state.profileError, actions.onRetryProfile)
        PLDivider(Modifier.padding(vertical = PLSpacing.lg))

        SettingsMenu(actions.onMenuItemClick)
        PLDivider(Modifier.padding(vertical = PLSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.sm)) {
            PLButton(
                text = stringResource(R.string.profile_logout),
                onClick = actions.onLogout,
                type = if (state.isLoading) PLButtonType.Loading else PLButtonType.Ghost,
            )
            PLButton(
                text = stringResource(R.string.profile_delete_account),
                onClick = actions.onDeleteAccount,
                type = PLButtonType.Destructive,
                enabled = !state.isLoading,
            )
        }
        Spacer(Modifier.height(PLSpacing.xxl))
    }

    if (state.confirmingDelete) {
        AlertDialog(
            onDismissRequest = actions.onDismissDelete,
            title = { Text(stringResource(R.string.profile_delete_title), style = PLFont.h2) },
            text = { Text(stringResource(R.string.profile_delete_message), style = PLFont.body) },
            confirmButton = {
                TextButton(onClick = actions.onConfirmDelete) {
                    Text(stringResource(R.string.profile_delete_confirm), color = PLColor.error, style = PLFont.button)
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissDelete) {
                    Text(stringResource(R.string.common_cancel), color = PLColor.textPrimary, style = PLFont.button)
                }
            },
            containerColor = PLColor.backgroundElevated,
            titleContentColor = PLColor.textPrimary,
            textContentColor = PLColor.textMuted,
        )
    }
}

private data class MenuEntry(val item: ProfileMenuItem, val icon: ImageVector, @StringRes val title: Int)

private val menuEntries = listOf(
    MenuEntry(ProfileMenuItem.EditProfile, Icons.Outlined.AccountCircle, R.string.profile_menu_edit),
    MenuEntry(ProfileMenuItem.Notifications, Icons.Outlined.Notifications, R.string.profile_menu_notifications),
    MenuEntry(ProfileMenuItem.Security, Icons.Outlined.Shield, R.string.profile_menu_security),
    MenuEntry(ProfileMenuItem.Help, Icons.AutoMirrored.Outlined.HelpOutline, R.string.profile_menu_help),
)

@Composable
private fun SettingsMenu(onClick: (ProfileMenuItem) -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Column(
        Modifier
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(1.dp, PLColor.borderSubtle, shape),
    ) {
        menuEntries.forEachIndexed { index, entry ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onClick(entry.item) }
                    .padding(PLSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PLSpacing.md),
            ) {
                Icon(entry.icon, contentDescription = null, tint = PLColor.goldMid, modifier = Modifier.size(20.dp))
                Text(stringResource(entry.title), style = PLFont.body, color = PLColor.textPrimary, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = PLColor.textDisabled)
            }
            if (index < menuEntries.lastIndex) {
                HorizontalDivider(color = PLColor.borderSubtle, modifier = Modifier.padding(start = PLSpacing.xl + 20.dp))
            }
        }
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    PaintresLumiereTheme {
        ProfileScreen(
            ProfileUiState(
                profile = UserProfile(id = "1", name = "Lucas", lastname = "Castro", email = "lucas@exemplo.com"),
                isProfileLoading = false,
            ),
            ProfileActions(),
        )
    }
}
