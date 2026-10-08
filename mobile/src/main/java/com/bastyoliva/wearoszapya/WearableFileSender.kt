package com.bastyoliva.wearoszapya

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.bastyoliva.wearoszapya.data.ConnectionStatus
import com.bastyoliva.wearoszapya.data.TransferRepository
import com.bastyoliva.wearoszapya.data.WearNode
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearableFileSender(private val context: Context) {

    suspend fun checkConnection() {
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val capabilityClient = Wearable.getCapabilityClient(context)

            val allNodes = nodeClient.connectedNodes.await()
            val capabilityInfo = capabilityClient
                .getCapability("wear_os_zapya_app", CapabilityClient.FILTER_ALL)
                .await()
            val reachableNodes = capabilityClient
                .getCapability("wear_os_zapya_app", CapabilityClient.FILTER_REACHABLE)
                .await().nodes

            val newNodes = allNodes.map { node ->
                val status = when {
                    !capabilityInfo.nodes.any { it.id == node.id } -> ConnectionStatus.APP_NOT_INSTALLED
                    !reachableNodes.any { it.id == node.id } -> ConnectionStatus.NOT_NEARBY
                    else -> ConnectionStatus.READY
                }
                WearNode(node.id, node.displayName, status)
            }

            TransferRepository.updateNodes(newNodes)

        } catch (e: Exception) {
            Log.e("WearableFileSender", e.stackTraceToString())
            TransferRepository.updateNodes(emptyList())
        }
    }

    fun sendFileToWear(uri: Uri) {
        val nodeId = TransferRepository.selectedNodeId ?: return
        val fileName = getFileName(uri) ?: context.getString(R.string.file_default_name)
        val intent = Intent(context, FileTransferService::class.java).apply {
            action = "ADD_TRANSFER"
            putExtra("file_uri", uri)
            putExtra("file_name", fileName)
            putExtra("target_node_id", nodeId)
        }
        context.startForegroundService(intent)
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
