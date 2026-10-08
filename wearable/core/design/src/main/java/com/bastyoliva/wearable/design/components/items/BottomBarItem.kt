package com.bastyoliva.wearable.design.components.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.bastyoliva.wearable.design.icons.Icons

@Composable
fun TransformingLazyColumnItemScope.BottomBarItem(
    transformationSpec: TransformationSpec,
    modifier: Modifier = Modifier,
    onUpClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 1.dp)
            .clickable(
                enabled = false,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ){}
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        onClick = {},
        enabled = false,
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {

            onUpClick?.let {
                FilledIconButton(onClick = onUpClick) {
                    Icon(
                        imageVector = Icons.ArrowBack,
                        contentDescription = null,
                    )
                }
            }

            onMoreClick?.let {
                FilledTonalIconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.MoreHorizontal,
                        contentDescription = null,
                    )
                }
            }
        }



    }
}


@Preview(device = "id:wearos_small_round", showBackground = true, showSystemUi = false)
@Composable
private fun BottomBarItemPreview() {
    val transformationSpec = rememberTransformationSpec()
    TransformingLazyColumn {
        item {
            BottomBarItem(
                transformationSpec = transformationSpec,
                onUpClick = {},
                onMoreClick = {}
            )
        }
    }
}
