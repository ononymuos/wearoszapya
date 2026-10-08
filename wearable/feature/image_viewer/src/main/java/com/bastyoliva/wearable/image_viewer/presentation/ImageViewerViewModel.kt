package com.bastyoliva.wearable.image_viewer.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
) : ViewModel() {

    var state by mutableStateOf<UiState>(UiState.Loading)
        private set

    fun setUriString(uriString: String){
        state = UiState.Success(uriString)
    }



}
