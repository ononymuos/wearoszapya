package com.bastyoliva.wearoszapya.presentation

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bastyoliva.wearoszapya.R

@Composable
fun TurboBoostDialog(
    onDismissRequest: () -> Unit,
    onOpenWatchWifi: () -> Unit,
    onWakeWatchWifi: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_bolt),
                contentDescription = null,
                tint = Color(0xFFFFB300),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.turbo_boost_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.turbo_boost_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "How to use Turbo Boost:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.turbo_boost_step1),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.turbo_boost_step2),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.turbo_boost_step3),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Quick Actions:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                // Button 1: Open Phone Hotspot
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent("android.settings.TETHER_SETTINGS").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open Hotspot settings", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_open_hotspot))
                }

                // Button 2: Open Watch Wi-Fi Settings
                OutlinedButton(
                    onClick = onOpenWatchWifi,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_open_watch_wifi))
                }

                // Button 3: Wake Watch Wi-Fi
                FilledTonalButton(
                    onClick = onWakeWatchWifi,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_wake_watch_wifi))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_dismiss))
            }
        }
    )
}
