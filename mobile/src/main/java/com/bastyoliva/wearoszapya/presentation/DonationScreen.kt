package com.bastyoliva.wearoszapya.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bastyoliva.wearoszapya.R

data class CryptoItem(
    val name: String,
    val network: String,
    val address: String,
    val qrResId: Int,
    val warning: String,
    val iconTint: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val cryptoList = remember {
        listOf(
            CryptoItem(
                name = "Ethereum",
                network = "ETH",
                address = "0xA54a491a40e57229f66Fa17f5C6c7f461b262c1d",
                qrResId = R.drawable.qr_eth,
                warning = "Only send Ethereum (ETH) network assets to this address. Other assets will be lost forever.",
                iconTint = Color(0xFF627EEA)
            ),
            CryptoItem(
                name = "Bitcoin",
                network = "BTC",
                address = "bc1q78zrnqxes8gdg8c9l34z4sg5258xpsz72at23x",
                qrResId = R.drawable.qr_btc,
                warning = "Only send Bitcoin (BTC) network assets to this address. Other assets will be lost forever.",
                iconTint = Color(0xFFF7931A)
            ),
            CryptoItem(
                name = "Tether",
                network = "USDT (TRC20)",
                address = "TVfsR7tHZvbKGK9gMasXFxcoGL43yNG6jV",
                qrResId = R.drawable.qr_usdt,
                warning = "Only send Tether (TRC20) network assets to this address. Other assets will be lost forever.",
                iconTint = Color(0xFF26A17B)
            )
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val currentCrypto = cryptoList[selectedIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support & Donations") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_content_description)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Turbo Boost Info Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bolt),
                        contentDescription = "Turbo Boost",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "⚡ WearOsZapya Turbo Engine",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Transfers seamlessly switch from Bluetooth to Wi-Fi / Hotspot. Files continue instantly from the exact byte where they left off!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Contact Email Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Developer Contact",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "basty.oliva2011@gmail.com",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:basty.oliva2011@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "WearOsZapya Feedback & Support")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Send Email")
                    }
                }
            }

            // Crypto Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                cryptoList.forEachIndexed { index, crypto ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        text = {
                            Text(
                                crypto.network,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Crypto Details Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Warning Banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = "⚠️ " + currentCrypto.warning,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // QR Code Image
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(currentCrypto.qrResId),
                            contentDescription = "${currentCrypto.name} QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentCrypto.name + " (" + currentCrypto.network + ") Address:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Address Text
                    SelectionContainer {
                        Text(
                            text = currentCrypto.address,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLowest,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy Address Button
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("${currentCrypto.network} Address", currentCrypto.address)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "${currentCrypto.network} address copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy Address")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
