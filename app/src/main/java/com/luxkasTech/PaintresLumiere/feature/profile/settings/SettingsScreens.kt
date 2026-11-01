package com.luxkasTech.PaintresLumiere.feature.profile.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luxkasTech.PaintresLumiere.BuildConfig
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButton
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLButtonType
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLInputField
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.data.settings.NotificationTopic

@Composable
private fun SettingsScaffold(@StringRes title: Int, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
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
            Text(stringResource(title), style = PLFont.h2, color = PLColor.textPrimary, modifier = Modifier.semantics { heading() })
        }
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(PLSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(PLSpacing.md),
            content = content,
        )
    }
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(PLRadius.card)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PLColor.backgroundElevated)
            .border(1.dp, PLColor.borderSubtle, shape),
        content = content,
    )
}

// region Notifications

private val topicLabels = mapOf(
    NotificationTopic.OrderStatus to (R.string.notifications_orders to R.string.notifications_orders_detail),
    NotificationTopic.Catalog to (R.string.notifications_catalog to R.string.notifications_catalog_detail),
    NotificationTopic.Promotions to (R.string.notifications_promotions to R.string.notifications_promotions_detail),
)

@Composable
fun NotificationsRoute(onBack: () -> Unit, viewModel: NotificationsViewModel = hiltViewModel()) {
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    SettingsScaffold(R.string.profile_menu_notifications, onBack) {
        Card {
            topicLabels.entries.forEachIndexed { i, (topic, labels) ->
                if (i > 0) HorizontalDivider(color = PLColor.borderSubtle)
                Row(Modifier.padding(PLSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(labels.first), style = PLFont.button, color = PLColor.textPrimary)
                        Text(stringResource(labels.second), style = PLFont.caption, color = PLColor.textMuted)
                    }
                    Switch(
                        checked = topic in enabled,
                        onCheckedChange = { viewModel.toggle(topic, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = PLColor.goldMid, checkedThumbColor = PLColor.backgroundPrimary),
                    )
                }
            }
        }
        Text(stringResource(R.string.notifications_local_note), style = PLFont.caption, color = PLColor.textDisabled)
    }
}

// endregion

// region Security

@Composable
fun SecurityRoute(onBack: () -> Unit, viewModel: ChangePasswordViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.events.collect { onBack() } }
    val password = KeyboardOptions(keyboardType = KeyboardType.Password)
    SettingsScaffold(R.string.profile_menu_security, onBack) {
        Text(stringResource(R.string.security_change_password), style = PLFont.button, color = PLColor.textPrimary)
        PLInputField(stringResource(R.string.security_current_password), state.current, viewModel::onCurrentChange, isSecure = true, keyboardOptions = password, errorMessage = state.currentError?.asString())
        PLInputField(stringResource(R.string.reset_new_password), state.new, viewModel::onNewChange, isSecure = true, keyboardOptions = password, errorMessage = state.newError?.asString())
        PLInputField(stringResource(R.string.auth_signup_field_confirm), state.confirm, viewModel::onConfirmChange, isSecure = true, keyboardOptions = password, errorMessage = state.confirmError?.asString())
        PLButton(stringResource(R.string.admin_form_save), onClick = viewModel::save, type = if (state.isSaving) PLButtonType.Loading else PLButtonType.Primary)
    }
}

// endregion

// region Help

@Composable
fun HelpRoute(onBack: () -> Unit) {
    val context = LocalContext.current
    val email = stringResource(R.string.support_email)
    val whatsapp = stringResource(R.string.support_whatsapp)
    val terms = stringResource(R.string.support_terms_url)
    val privacy = stringResource(R.string.support_privacy_url)
    SettingsScaffold(R.string.profile_menu_help, onBack) {
        Text(stringResource(R.string.help_contact), style = PLFont.button, color = PLColor.textPrimary)
        if (email.isBlank() && whatsapp.isBlank()) {
            Text(stringResource(R.string.help_contact_pending), style = PLFont.body, color = PLColor.textMuted)
        } else {
            Card {
                if (email.isNotBlank()) LinkRow(stringResource(R.string.help_email), email) { context.open(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))) }
                if (whatsapp.isNotBlank()) LinkRow(stringResource(R.string.help_whatsapp), whatsapp) { context.open(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/${whatsapp.filter(Char::isDigit)}"))) }
            }
        }
        Text(stringResource(R.string.help_faq), style = PLFont.button, color = PLColor.textPrimary, modifier = Modifier.padding(top = PLSpacing.sm))
        Card {
            listOf(
                R.string.help_faq_production_q to R.string.help_faq_production_a,
                R.string.help_faq_payment_q to R.string.help_faq_payment_a,
                R.string.help_faq_tracking_q to R.string.help_faq_tracking_a,
            ).forEachIndexed { i, (q, a) ->
                if (i > 0) HorizontalDivider(color = PLColor.borderSubtle)
                Column(Modifier.padding(PLSpacing.md)) {
                    Text(stringResource(q), style = PLFont.button, color = PLColor.textPrimary)
                    Text(stringResource(a), style = PLFont.body, color = PLColor.textMuted)
                }
            }
        }
        if (terms.isNotBlank() || privacy.isNotBlank()) {
            Card {
                if (terms.isNotBlank()) LinkRow(stringResource(R.string.help_terms), null) { context.open(Intent(Intent.ACTION_VIEW, Uri.parse(terms))) }
                if (privacy.isNotBlank()) LinkRow(stringResource(R.string.help_privacy), null) { context.open(Intent(Intent.ACTION_VIEW, Uri.parse(privacy))) }
            }
        }
        Text(
            stringResource(R.string.help_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            style = PLFont.caption,
            color = PLColor.textDisabled,
        )
    }
}

@Composable
private fun LinkRow(label: String, value: String?, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(PLSpacing.md),
    ) {
        Text(label, style = PLFont.body, color = PLColor.textPrimary, modifier = Modifier.weight(1f))
        value?.let { Text(it, style = PLFont.caption, color = PLColor.goldMid) }
    }
}

/** Opens an external app; a missing target app is not a crash. */
private fun Context.open(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        android.widget.Toast.makeText(this, getString(R.string.help_no_app), android.widget.Toast.LENGTH_SHORT).show()
    }
}

// endregion
