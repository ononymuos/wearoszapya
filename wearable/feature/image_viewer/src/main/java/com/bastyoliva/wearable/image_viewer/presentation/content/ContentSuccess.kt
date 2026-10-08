package com.bastyoliva.wearable.image_viewer.presentation.content

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ScreenScaffold
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun ContentSuccess(
    uriString: String
) {
    val context = LocalContext.current

    val zoomState = rememberZoomState()
    ScreenScaffold(
        contentPadding = PaddingValues(44.dp)
    ){ contentPadding ->
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uriString)
                .size(Size.ORIGINAL)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState)
                .padding(contentPadding)
        )
    }

}

@Composable
@Preview(device = "id:wearos_small_round")
fun ContentSuccessPreview() {
    ContentSuccess("")
}
