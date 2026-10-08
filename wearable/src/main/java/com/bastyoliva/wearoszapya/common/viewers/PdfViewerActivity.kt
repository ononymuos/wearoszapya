package com.bastyoliva.wearoszapya.common.viewers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.AppScaffold
import com.bastyoliva.wearable.design.theme.WearOsZapyaTheme
import com.bastyoliva.wearable.pdf_viewer.PdfViewerRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PdfViewerActivity : ComponentActivity() {

    private var incomingFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data!!
            val mimeType = contentResolver.getType(uri)
            if (mimeType == "application/pdf" || (mimeType == null && uri.path?.endsWith(".pdf") == true)) {
                incomingFileUri = uri
            } else {
                finish()
                return
            }
        } else {
            finish()
            return
        }


        setContent {
            WearOsZapyaTheme {
                AppScaffold {
                    incomingFileUri?.let {
                        PdfViewerRoute(it.toString())
                    }
                }

            }
        }
    }


}
