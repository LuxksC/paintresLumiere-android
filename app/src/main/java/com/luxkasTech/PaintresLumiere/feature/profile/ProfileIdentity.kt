package com.luxkasTech.PaintresLumiere.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.common.UiText
import com.luxkasTech.PaintresLumiere.core.designsystem.components.PLRemoteImage
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLRadius
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLSpacing
import com.luxkasTech.PaintresLumiere.data.profile.UserProfile

/** Avatar + name + email. While loading it shows skeleton bars — never placeholder identity text. */
@Composable
internal fun ProfileIdentity(
    profile: UserProfile?,
    isLoading: Boolean,
    error: UiText?,
    onRetry: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(PLSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        Avatar(profile?.image)
        when {
            profile != null -> Column(verticalArrangement = Arrangement.spacedBy(PLSpacing.xs)) {
                Text(profile.displayName, style = PLFont.h2, color = PLColor.textPrimary)
                Text(profile.email, style = PLFont.body, color = PLColor.textMuted)
            }
            isLoading -> {
                val loading = stringResource(R.string.common_loading)
                Column(
                    verticalArrangement = Arrangement.spacedBy(PLSpacing.sm),
                    modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = loading },
                ) {
                    SkeletonBar(160)
                    SkeletonBar(120)
                }
            }
            error != null -> Column {
                Text(error.asString(), style = PLFont.body, color = PLColor.textMuted)
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.common_try_again), style = PLFont.link, color = PLColor.goldMid)
                }
            }
        }
    }
}

@Composable
private fun Avatar(imageUrl: String?) {
    val modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(PLColor.backgroundElevated)
        .border(1.dp, PLColor.goldAntique, CircleShape)
    if (imageUrl.isNullOrBlank()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = PLColor.goldMid, modifier = Modifier.size(28.dp))
        }
    } else {
        PLRemoteImage(url = imageUrl, contentDescription = stringResource(R.string.profile_avatar), modifier = modifier)
    }
}

@Composable
private fun SkeletonBar(widthDp: Int) {
    Box(
        Modifier
            .width(widthDp.dp)
            .height(14.dp)
            .background(PLColor.backgroundHighlight, RoundedCornerShape(PLRadius.badge)),
    )
}
