package com.bastyoliva.wearoszapya

import android.util.Log
import com.bastyoliva.wearoszapya.data.TransferMode
import com.bastyoliva.wearoszapya.data.TransferRepository
import com.bastyoliva.wearoszapya.data.TransferStatus
import com.bastyoliva.wearoszapya.data.TurboConnectionInfo
import com.bastyoliva.wearoszapya.data.TurboConstants
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class TransferStatusListener : WearableListenerService() {

    companion object {
        private const val TAG = "TransferStatusListener"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            TurboConstants.PATH_BOOST_READY -> {
                try {
                    val raw = String(messageEvent.data)
                    Log.d(TAG, "Received boost ready payload: $raw")
                    val parts = raw.split("|")
                    if (parts.size >= 4) {
                        val transferId = parts[0]
                        val ip = parts[1]
                        val port = parts[2].toIntOrNull() ?: TurboConstants.TURBO_PORT
                        val offset = parts[3].toLongOrNull() ?: 0L
                        val info = TurboConnectionInfo(transferId, ip, port, offset)
                        TransferRepository.signalTurboReady(info)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling boost ready: ${e.message}", e)
                }
            }

            TurboConstants.PATH_BOOST_FALLBACK -> {
                try {
                    val raw = String(messageEvent.data)
                    Log.d(TAG, "Received boost fallback payload: $raw")
                    val parts = raw.split("|")
                    val transferId = parts.getOrNull(0)
                    if (transferId != null) {
                        TransferRepository.updateItem(transferId) {
                            it.copy(mode = TransferMode.BLUETOOTH, isBoostActive = false, speedText = "~30 KB/s")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling boost fallback: ${e.message}", e)
                }
            }

            TurboConstants.PATH_TRANSFER_STATUS, "/file-transfer-status" -> {
                val message = String(messageEvent.data)
                val parts = message.split(":", limit = 2)
                if (parts.size == 2) {
                    val status = parts[0]
                    val fileName = parts[1]
                    val sourceNodeId = messageEvent.sourceNodeId
                    
                    val notificationHelper = NotificationHelper(this)
                    if (status == "success") {
                        TransferRepository.queue.find { it.fileName == fileName && it.targetNodeId == sourceNodeId && it.status == TransferStatus.SENDING }?.let { item ->
                            TransferRepository.updateItem(item.id) {
                                it.copy(status = TransferStatus.SUCCESS, progress = 100, speedText = "Complete")
                            }
                        }
                        notificationHelper.showTransferNotification(fileName, TransferStatus.SUCCESS)
                    } else {
                        TransferRepository.queue.find { it.fileName == fileName && it.targetNodeId == sourceNodeId && it.status == TransferStatus.SENDING }?.let { item ->
                            TransferRepository.updateItem(item.id) { it.copy(status = TransferStatus.ERROR) }
                        }
                        notificationHelper.showTransferNotification(fileName, TransferStatus.ERROR)
                    }
                }
            }
        }
    }
}
