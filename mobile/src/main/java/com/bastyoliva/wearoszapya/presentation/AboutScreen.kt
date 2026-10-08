package com.bastyoliva.wearoszapya.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bastyoliva.wearoszapya.R
import com.bastyoliva.wearoszapya.ui.theme.WearOsZapyaTheme
import com.github.droibit.oss_licenses.ui.compose.material3.OssLicensesActivity

data class AboutItem(
    val text: String,
    val icon: Painter? = null,
    val iconTint: Color? = null,
    val onClick: () -> Unit = {}
)

private fun openIntentSafely(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.error_no_app_to_open_link),
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onDonateClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val items = listOf(
        AboutItem(
            text = "Crypto Donations (ETH, BTC, USDT)",
            icon = painterResource(R.drawable.ic_attach_money),
            iconTint = Color(0xFF4CAF50),
            onClick = onDonateClick
        ),
        AboutItem(
            text = "Developer: basty.oliva2011@gmail.com",
            icon = painterResource(R.drawable.ic_article),
            onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:basty.oliva2011@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "WearOsZapya Support & Inquiries")
                }
                openIntentSafely(context, intent)
            }
        ),
        AboutItem(
            text = "⚡ Turbo Boost Mode Info",
            icon = painterResource(R.drawable.ic_bolt),
            iconTint = Color(0xFFFFB300),
            onClick = onDonateClick
        ),
        AboutItem(
            text = stringResource(R.string.about_oss_licenses),
            icon = painterResource(R.drawable.ic_article),
            onClick = {
                openIntentSafely(context, OssLicensesActivity.createIntent(context))
            }
        )
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.mobile_app_name))
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_content_description)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->

        val colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )

        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(items) { index, item ->
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = items.count()),
                    colors = colors,
                    onClick = item.onClick,
                    leadingContent = {
                        item.icon?.let { icon ->
                            Icon(
                                painter = icon,
                                contentDescription = null,
                                tint = item.iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    content = { Text(item.text) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    WearOsZapyaTheme {
        AboutScreen()
    }
}