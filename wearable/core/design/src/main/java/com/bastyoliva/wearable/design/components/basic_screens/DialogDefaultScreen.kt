package com.bastyoliva.wearable.design.components.basic_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.bastyoliva.wearable.design.icons.Icons

@Composable
fun DialogDefaultScreen(
    title: String? = null,
    onCancel: () -> Unit,
    onOk: () -> Unit,
    cancelIcon: ImageVector,
    okIcon: ImageVector
){

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {

            title?.let{
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Row{

                FilledIconButton(
                    onClick = {
                        onCancel()
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = cancelIcon,
                        contentDescription = null,
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        onOk()
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = okIcon,
                        contentDescription = null,
                    )
                }
            }

        }
    }
    }


@Composable
@Preview(device = "id:wearos_small_round", showSystemUi = false, showBackground = true)
fun DialogDefaultScreenPreview(){
    DialogDefaultScreen(
        title = "viojvdiojv",
        onCancel = {},
        onOk = {},
        cancelIcon = Icons.ArrowBack,
        okIcon = Icons.ArrowBack
    )
}
