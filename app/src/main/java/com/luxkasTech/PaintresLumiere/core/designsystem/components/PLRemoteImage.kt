package com.luxkasTech.PaintresLumiere.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor

/**
 * The one way to render a remote image — mirrors `PLRemoteImage` (Kingfisher) on iOS. Falls back to
 * [PLImagePlaceholder] while loading, on error, and when [url] is null.
 */
@Composable
fun PLRemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (url.isNullOrBlank()) {
        PLImagePlaceholder(modifier)
        return
    }
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(200)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = { PLImagePlaceholder(Modifier.fillMaxSize()) },
        error = { PLImagePlaceholder(Modifier.fillMaxSize()) },
    )
}

@Composable
fun PLImagePlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.background(PLColor.backgroundElevated), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = PLColor.goldAntique,
            modifier = Modifier.size(32.dp),
        )
    }
}
