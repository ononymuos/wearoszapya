package com.bastyoliva.wearable.onboarding.presentation.content

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.bastyoliva.wearable.design.components.common.rememberSafeRotaryScrollableBehavior
import com.bastyoliva.wearable.design.components.items.BadgedTextItem
import com.bastyoliva.wearable.design.components.items.CodeItem
import com.bastyoliva.wearable.design.components.items.TextItem
import com.bastyoliva.wearable.design.icons.Icons
import com.bastyoliva.wearable.onboarding.R
import com.bastyoliva.wearable.onboarding.presentation.Event
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding
import androidx.core.net.toUri

@Composable
fun ContentFailed(onEvent: (Event) -> Unit){

    val columnState = rememberTransformingLazyColumnState()
    val contentPadding = rememberResponsiveColumnPadding(
        first = ColumnItemType.BodyText,
        last = ColumnItemType.Button,
    )

    val transformationSpec = rememberTransformationSpec()
    val context = LocalContext.current
    val errorSettingsText = stringResource(R.string.error_opening_settings)

    ScreenScaffold(
        scrollState = columnState, contentPadding = contentPadding
    )
    { contentPadding ->

        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            rotaryScrollableBehavior = rememberSafeRotaryScrollableBehavior(columnState)
        ) {


            item {
                TextItem(
                    transformationSpec, text = stringResource(R.string.permission_alert)
                )
            }

            item {
                BadgedTextItem(
                    transformationSpec, text = stringResource(R.string.pt1), badgeText = "1"
                )
            }

            item {
                BadgedTextItem(
                    transformationSpec, text = stringResource(R.string.pt2), badgeText = "2"
                )
            }

            item {
                CodeItem(
                    transformationSpec, text = stringResource(R.string.pt2_command)
                )
            }

            item {
                BadgedTextItem(
                    transformationSpec, text = stringResource(R.string.pt3), badgeText = "3"
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                FilledTonalButton(
                    onClick = {
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                        } else {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                                } catch (_: Exception) {
                                    try {
                                        val detailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = "package:${context.packageName}".toUri()
                                        }
                                        context.startActivity(detailsIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, errorSettingsText, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    icon = {
                        Icon(
                            imageVector = Icons.Settings,
                            contentDescription = "",
                        )
                    }) {
                    Text(
                        text = stringResource(R.string.try_grant_permissions),
                        modifier = Modifier.padding(4.dp),
                        fontSize = 12.sp
                    )
                }
            }

            item {
                FilledTonalButton(
                    onClick = { onEvent(Event.OnOpenLinkOnPhone) },
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    icon = {
                        Icon(
                            imageVector = Icons.MobileArrowRight,
                            contentDescription = "",
                        )
                    }) {
                    Text(
                        text = stringResource(R.string.info),
                        modifier = Modifier.padding(4.dp),
                        fontSize = 12.sp
                    )
                }
            }

        }

    }

}

@Composable
@Preview(device = "id:wearos_small_round", showSystemUi = false)
fun ContentFailedPreview(){
    ContentFailed({})
}