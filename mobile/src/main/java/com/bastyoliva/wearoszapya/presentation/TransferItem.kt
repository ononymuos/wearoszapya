package com.bastyoliva.wearoszapya.presentation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bastyoliva.wearoszapya.R
import com.bastyoliva.wearoszapya.data.TransferItem
import com.bastyoliva.wearoszapya.data.TransferMode
import com.bastyoliva.wearoszapya.data.TransferRepository
import com.bastyoliva.wearoszapya.data.TransferStatus
import com.bastyoliva.wearoszapya.ui.theme.WearOsZapyaTheme
import com.materialkolor.ktx.harmonize
import kotlin.math.abs

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TransferItem(
    modifier: Modifier = Modifier,
    item: TransferItem,
    onCancel: () -> Unit,
    index: Int = 1,
    count: Int = 1,
) {
    val context = LocalContext.current
    val successColor = colorResource(R.color.success_container).harmonize(MaterialTheme.colorScheme.primary)
    val errorColor = MaterialTheme.colorScheme.errorContainer
    val defaultColor = MaterialTheme.colorScheme.surfaceContainer

    val onSuccessColor = colorResource(R.color.on_success_container).harmonize(MaterialTheme.colorScheme.primary)
    val onErrorColor = MaterialTheme.colorScheme.onErrorContainer
    val onDefaultColor = MaterialTheme.colorScheme.onSurface

    val progress by animateFloatAsState(
        item.progress / 100f
    )

    val bgColor by animateColorAsState(
        when (item.status) {
            TransferStatus.PENDING -> defaultColor
            TransferStatus.SENDING -> defaultColor
            TransferStatus.SUCCESS -> successColor
            TransferStatus.ERROR -> errorColor
        }
    )

    val contentColor by animateColorAsState(
        when (item.status) {
            TransferStatus.PENDING -> onDefaultColor
            TransferStatus.SENDING -> onDefaultColor
            TransferStatus.SUCCESS -> onSuccessColor
            TransferStatus.ERROR -> onErrorColor
        }
    )

    val colors = ListItemDefaults.colors(containerColor = bgColor)
    val dismissState = rememberSwipeToDismissBoxState()
    var isVisible by remember { mutableStateOf(true) }

    if (isVisible) {
        val density = LocalDensity.current
        val offset = try { dismissState.requireOffset() } catch (_: Exception) { 0f }
        val dynamicWidth = with(density) { abs(offset).toDp() }
        val thresholdPx = with(density) { 80.dp.toPx() }
        val fraction = (abs(offset) / thresholdPx).coerceIn(0f, 1f)

        val direction = dismissState.dismissDirection
        val isToEnd = direction == SwipeToDismissBoxValue.StartToEnd

        SwipeToDismissBox(
            modifier = modifier,
            state = dismissState,
            backgroundContent = {
                Box(Modifier.fillMaxSize()) {
                    if (direction != SwipeToDismissBoxValue.Settled) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .align(if (isToEnd) Alignment.CenterStart else Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(dynamicWidth)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .alpha(fraction)
                            )
                        }
                    }
                }
            },
            onDismiss = { _ ->
                isVisible = false
                TransferRepository.removeItem(item.id)
            },
        ) {
            SegmentedListItem(
                shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
                colors = colors,
                contentPadding = PaddingValues(),
                content = {
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 10.dp)
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = contentColor
                            )
                            
                            // Status & Speed line
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = when (item.status) {
                                        TransferStatus.PENDING -> stringResource(R.string.status_pending)
                                        TransferStatus.SENDING -> if (item.progress < 100) "${item.progress}%" else stringResource(R.string.status_waiting_watch)
                                        TransferStatus.SUCCESS -> stringResource(R.string.status_success)
                                        TransferStatus.ERROR -> stringResource(R.string.status_error)
                                    },
                                    style = MaterialTheme.typography.labelLargeEmphasized,
                                    color = contentColor
                                )

                                if (item.status == TransferStatus.SENDING && item.speedText.isNotEmpty()) {
                                    Text(
                                        text = "• ${item.speedText}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.mode == TransferMode.TURBO_WIFI) Color(0xFFFFB300) else contentColor
                                    )
                                }
                            }

                            // Mode Badge
                            if (item.status == TransferStatus.SENDING) {
                                val isTurbo = item.mode == TransferMode.TURBO_WIFI
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isTurbo) Color(0x33FFB300) else MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isTurbo) "⚡ Turbo Wi-Fi Active" else "🔵 Bluetooth (~30 KB/s)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isTurbo) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Boost Icon button (when sending and in bluetooth mode)
                        if (item.status == TransferStatus.SENDING && item.mode == TransferMode.BLUETOOTH) {
                            IconButton(
                                onClick = {
                                    TransferRepository.triggerBoost(item.id)
                                    Toast.makeText(
                                        context,
                                        "⚡ Turbo Boost triggered! Switching to high-speed Wi-Fi/Hotspot...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x22FFB300))
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_bolt),
                                    contentDescription = "Turbo Boost",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Action / Progress Widget
                        Crossfade(
                            targetState = item.status,
                            label = "status_crossfade"
                        ) { status ->
                            when (status) {
                                TransferStatus.PENDING -> {
                                    CircularWavyProgressIndicator(
                                        modifier = Modifier.size(48.dp),
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable(onClick = onCancel)
                                            .padding(12.dp)
                                    )
                                }
                                TransferStatus.SENDING -> {
                                    CircularWavyProgressIndicator(
                                        progress = { progress },
                                        stroke = WavyProgressIndicatorDefaults.circularTrackStroke,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .clickable(onClick = onCancel)
                                            .padding(12.dp)
                                    )
                                }
                                TransferStatus.SUCCESS -> {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_check),
                                        contentDescription = null,
                                        tint = bgColor,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(MaterialShapes.Pill.toShape())
                                            .background(contentColor)
                                            .padding(12.dp)
                                    )
                                }
                                TransferStatus.ERROR -> {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_close),
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(MaterialShapes.Cookie9Sided.toShape())
                                            .clickable(
                                                onClick = { TransferRepository.removeItem(item.id) }
                                            )
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransferItemPreview() {
    WearOsZapyaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransferItem(
                item = TransferItem(
                    id = "1",
                    targetNodeId = "node1",
                    uri = Uri.EMPTY,
                    fileName = "movie.mp4",
                    progress = 0,
                    status = TransferStatus.PENDING
                ),
                onCancel = {},
                count = 4,
                index = 0
            )
            TransferItem(
                item = TransferItem(
                    id = "2",
                    targetNodeId = "node1",
                    uri = Uri.EMPTY,
                    fileName = "high_res_photo.jpg",
                    progress = 45,
                    status = TransferStatus.SENDING,
                    mode = TransferMode.TURBO_WIFI,
                    speedText = "⚡ 18.5 MB/s"
                ),
                onCancel = {},
                count = 4,
                index = 1
            )
        }
    }
}
