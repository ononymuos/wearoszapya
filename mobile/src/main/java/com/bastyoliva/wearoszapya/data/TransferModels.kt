package com.bastyoliva.wearoszapya.data

import android.net.Uri

enum class TransferStatus {
    PENDING,
    SENDING,
    SUCCESS,
    ERROR
}

enum class TransferMode {
    BLUETOOTH,
    TURBO_WIFI
}

data class TransferItem(
    val id: String,
    val targetNodeId: String,
    val uri: Uri,
    val fileName: String,
    val progress: Int = 0,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val speedText: String = "",
    val status: TransferStatus = TransferStatus.PENDING,
    val mode: TransferMode = TransferMode.BLUETOOTH,
    val isBoostActive: Boolean = false
)

enum class ConnectionStatus {
    NOT_CONNECTED,
    NOT_NEARBY,
    APP_NOT_INSTALLED,
    READY,
}

data class WearNode(
    val id: String,
    val name: String,
    val status: ConnectionStatus
)

data class TurboConnectionInfo(
    val transferId: String,
    val ip: String,
    val port: Int,
    val confirmedOffset: Long
)
