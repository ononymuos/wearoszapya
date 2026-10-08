package com.bastyoliva.wearoszapya.presentation

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bastyoliva.wearoszapya.WearableFileSender
import com.bastyoliva.wearoszapya.data.TransferRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var sharedUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    private val sender = WearableFileSender(application)

    init {
        monitorConnection()
    }

    private fun monitorConnection() {
        viewModelScope.launch {
            while (true) {
                sender.checkConnection()
                delay(3.seconds)
            }
        }
    }

    fun updateSharedUris(uris: List<Uri>) {
        sharedUris = uris
    }

    fun onNodeSelected(nodeId: String) {
        TransferRepository.selectedNodeId = nodeId
    }
}
