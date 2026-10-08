package com.bastyoliva.wearable.design.components.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.LocalTextStyle
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

@Composable
fun TransformingLazyColumnItemScope.CodeItem(
    transformationSpec: TransformationSpec,
    modifier: Modifier = Modifier,
    text: String
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

        SelectionContainer{
            Text(
                text = text,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = 12.sp,
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace
                )
            )
        }

    }
}


@Preview(device = "id:wearos_small_round", showBackground = true, showSystemUi = false)
@Composable
private fun CodeItemPreview() {
    val transformationSpec = rememberTransformationSpec()
    TransformingLazyColumn {
        item {
            CodeItem(
                transformationSpec = transformationSpec,
                text = "adb shell appops set --uid com.bastyoliva.wearoszapya MANAGE_EXTERNAL_STORAGE allow",
            )
        }
    }
}
