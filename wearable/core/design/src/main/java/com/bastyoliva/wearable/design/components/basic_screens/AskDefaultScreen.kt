package com.bastyoliva.wearable.design.components.basic_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.bastyoliva.wearable.design.components.common.rememberSafeRotaryScrollableBehavior
import com.bastyoliva.wearable.design.components.items.ContentItem
import com.bastyoliva.wearable.design.icons.Icons
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding

@Composable
fun AskDefaultScreen(
    title: String? = null,
    content: List<@Composable (Modifier) -> Unit>
){
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){

        val columnState = rememberTransformingLazyColumnState()
        val contentPadding = rememberResponsiveColumnPadding(
            first = ColumnItemType.ListHeader,
            last = ColumnItemType.Button,
        )

        val transformationSpec = rememberTransformationSpec()

        ScreenScaffold(
            scrollState = columnState, contentPadding = contentPadding
        )
        { contentPadding ->

            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
                modifier = Modifier.padding(top = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                rotaryScrollableBehavior = rememberSafeRotaryScrollableBehavior(columnState)
            ) {

                title?.let {

                    item {
                        ContentItem(
                            transformationSpec = transformationSpec
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = title,
                            )
                        }
                    }
                }

                for (contentItem in content){
                    item {
                        ContentItem(
                            transformationSpec = transformationSpec
                        ) {
                            contentItem(Modifier.fillMaxWidth())
                        }
                    }
                }



            }
        }

    }
}

@Composable
@Preview(device = "id:wearos_large_round", showBackground = true, showSystemUi = false)
fun AskDefaultScreenPreview(){
    AskDefaultScreen(
        title = "Пример",
        content = listOf(
            {    modifier ->
                Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
                    FilledIconButton(
                        onClick = {},
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }
            }
        )
        )

}
