package com.bastyoliva.wearoszapya

import com.bastyoliva.wearoszapya.data.ConnectionStatus
import com.bastyoliva.wearoszapya.data.TransferItem
import com.bastyoliva.wearoszapya.data.TransferMode
import com.bastyoliva.wearoszapya.data.TransferRepository
import com.bastyoliva.wearoszapya.data.TransferStatus
import com.bastyoliva.wearoszapya.data.TurboConnectionInfo
import com.bastyoliva.wearoszapya.data.TurboConstants
import com.bastyoliva.wearoszapya.data.WearNode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TurboConstantsAndProtocolTest {

    @Test
    fun testProtocolConstantsIntegrity() {
        assertEquals("wear_os_zapya_app", TurboConstants.CAPABILITY_NAME)
        assertEquals(8988, TurboConstants.TURBO_PORT)
        assertEquals("/zapya/open-app", TurboConstants.PATH_OPEN_APP)
        assertEquals("/zapya/open-wifi-settings", TurboConstants.PATH_OPEN_WIFI_SETTINGS)
        assertEquals("/zapya/open-hotspot-settings", TurboConstants.PATH_OPEN_HOTSPOT_SETTINGS)
        assertEquals("/zapya/boost/wake-wifi", TurboConstants.PATH_BOOST_WAKE_WIFI)
        assertEquals("/zapya/boost/status", TurboConstants.PATH_BOOST_STATUS)
        assertEquals("/zapya/boost/request", TurboConstants.PATH_BOOST_REQUEST)
        assertEquals("/zapya/boost/ready", TurboConstants.PATH_BOOST_READY)
        assertEquals("/zapya/boost/fallback", TurboConstants.PATH_BOOST_FALLBACK)
        assertEquals("wearoszapya://open", TurboConstants.URI_SCHEME_OPEN)
        assertEquals(32768, TurboConstants.BUFFER_SIZE_BLUETOOTH)
        assertEquals(65536, TurboConstants.BUFFER_SIZE_TURBO)
    }

    @Test
    fun testTransferRepositoryQueueAndTurboSwitchFlow() = runBlocking {
        val testItem = TransferItem(
            id = "test-transfer-item-1",
            targetNodeId = "watch-node-01",
            uri = org.mockito.Mockito.mock(android.net.Uri::class.java),
            fileName = "sample_photo.jpg",
            progress = 20,
            bytesTransferred = 204800L,
            totalBytes = 1048576L,
            status = TransferStatus.SENDING,
            mode = TransferMode.BLUETOOTH,
            isBoostActive = false
        )

        TransferRepository.addItem(testItem)
        assertTrue(TransferRepository.queue.any { it.id == "test-transfer-item-1" })

        // Switch to Turbo Wi-Fi mode
        TransferRepository.updateItem("test-transfer-item-1") {
            it.copy(
                mode = TransferMode.TURBO_WIFI,
                isBoostActive = true,
                speedText = "⚡ Turbo Active"
            )
        }

        val updated = TransferRepository.queue.first { it.id == "test-transfer-item-1" }
        assertEquals(TransferMode.TURBO_WIFI, updated.mode)
        assertTrue(updated.isBoostActive)
        assertEquals("⚡ Turbo Active", updated.speedText)

        // Test turbo ready signal emission
        val info = TurboConnectionInfo(
            transferId = "test-transfer-item-1",
            ip = "192.168.43.100",
            port = 8988,
            confirmedOffset = 204800L
        )
        TransferRepository.signalTurboReady(info)

        // Verify clean item removal
        TransferRepository.removeItem("test-transfer-item-1")
        assertFalse(TransferRepository.queue.any { it.id == "test-transfer-item-1" })
    }

    @Test
    fun testWearNodeSelectionLogic() {
        val node1 = WearNode("node-a", "Galaxy Watch 4", ConnectionStatus.NOT_CONNECTED)
        val node2 = WearNode("node-b", "Pixel Watch 2", ConnectionStatus.READY)
        
        TransferRepository.updateNodes(listOf(node1, node2))
        assertEquals("node-b", TransferRepository.selectedNodeId)
        assertEquals(ConnectionStatus.READY, TransferRepository.connectionStatus)
    }
}
