package com.bastyoliva.wearable.design.components.basic_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import com.bastyoliva.wearable.design.components.common.rememberSafeRotaryScrollableBehavior
import com.bastyoliva.wearable.design.components.items.TextItem
import com.bastyoliva.wearable.design.icons.Icons
import com.google.android.horologist.compose.layout.ColumnItemType
import com.google.android.horologist.compose.layout.rememberResponsiveColumnPadding

@Composable
fun ContentFailedDefaultScreen(
    title: String? = null,
    onBackClick: () -> Unit,
){
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){

        val columnState = rememberTransformingLazyColumnState()
        val contentPadding = rememberResponsiveColumnPadding(
            first = ColumnItemType.BodyText,
            last = ColumnItemType.Button,
        )

        val transformationSpec = rememberTransformationSpec()

        ScreenScaffold(
            scrollState = columnState, contentPadding = contentPadding
        )
        {
            contentPadding ->

            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
                modifier = Modifier.padding(top = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                rotaryScrollableBehavior = rememberSafeRotaryScrollableBehavior(columnState)
            ) {

                title?.let {
                    item {
                        TextItem(
                            transformationSpec = transformationSpec,
                            text = title,
                            textColor = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            textStyle = MaterialTheme.typography.titleSmall
                        )
                    }
                }


                item {
                    FilledTonalIconButton(
                        onClick = {
                            onBackClick()
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                }


            }
        }

    }
}