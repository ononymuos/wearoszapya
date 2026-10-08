package com.bastyoliva.wearoszapya.common.viewers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.AppScaffold
import com.bastyoliva.wearable.design.theme.WearOsZapyaTheme
import com.bastyoliva.wearable.text_viewer.TextViewerRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TextViewerActivity : ComponentActivity() {

    private var incomingFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data!!
            val mimeType = contentResolver.getType(uri)
            if (mimeType != null && mimeType.startsWith("text/")) {
                incomingFileUri = uri
            } else {
                finish()
            }
        }


        setContent {
            WearOsZapyaTheme {
                AppScaffold {
                    TextViewerRoute(incomingFileUri.toString())
                }

            }
        }
    }


}
