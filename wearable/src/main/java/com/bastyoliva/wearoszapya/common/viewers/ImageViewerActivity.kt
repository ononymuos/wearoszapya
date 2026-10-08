package com.bastyoliva.wearoszapya.common.viewers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.AppScaffold
import com.bastyoliva.wearable.design.theme.WearOsZapyaTheme
import com.bastyoliva.wearable.image_viewer.ImageViewerRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageViewerActivity : ComponentActivity() {

    private var incomingFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data!!
            incomingFileUri = uri
        }


        setContent {
            WearOsZapyaTheme {
                AppScaffold {
                    ImageViewerRoute(incomingFileUri.toString())
                }

            }
        }
    }


}
