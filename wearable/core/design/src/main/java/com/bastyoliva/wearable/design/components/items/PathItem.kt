package com.bastyoliva.wearable.design.components.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.bastyoliva.wearable.design.icons.Icons

@Composable
fun TransformingLazyColumnItemScope.PathItem(
    transformationSpec: TransformationSpec,
    modifier: Modifier = Modifier,
    homeIcon: ImageVector? = null,
    fullPath: String,
    homePath: String,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(fullPath) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val cleanedPath = fullPath.removePrefix(homePath).trimStart('/')
    val segments = cleanedPath.split("/").filter { it.isNotEmpty() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 1.dp)
            .clickable(enabled = false, indication = null, interactionSource = remember { MutableInteractionSource() }) {}
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        onClick = {},
        enabled = false,
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(modifier = Modifier.width(4.dp))

            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 4.dp),
                maxItemsInEachRow = Int.MAX_VALUE,
            ) {
                if (homeIcon != null) {
                    Icon(
                        imageVector = homeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically)
                    )
                } else {
                    Text(
                        text = homePath.substringAfterLast('/'),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                segments.forEach { segment ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Icon(
                            imageVector = Icons.DoubleArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(horizontal = 2.dp, vertical = 6.dp)
                        )

                        Text(
                            text = segment,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                }
            }
        }
    }
}

@Preview(device = "id:wearos_small_round", showBackground = true, showSystemUi = false)
@Composable
private fun PathItemPreview() {
    val transformationSpec = rememberTransformationSpec()
    TransformingLazyColumn {
        item {
            PathItem(
                transformationSpec = transformationSpec,
                homeIcon = Icons.Watch,
                fullPath = "/storage/emulated/0/Download/Folder/Sub",
                homePath = "/storage/emulated/0",
            )
        }
    }
}
