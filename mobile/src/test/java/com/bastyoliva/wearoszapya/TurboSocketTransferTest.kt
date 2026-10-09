package com.bastyoliva.wearoszapya

import com.bastyoliva.wearoszapya.data.TurboConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean

class TurboSocketTransferTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testTurboBoostHandshakeAndFullTransfer() = runBlocking {
        val testDataSize = 1024 * 512 // 512 KB
        val originalData = ByteArray(testDataSize).also { Random(42).nextBytes(it) }
        val targetFile = tempFolder.newFile("received_test_file.bin")

        val server = ServerSocket(0)
        val port = server.localPort

        val serverJob = async(Dispatchers.IO) {
            val clientSocket = server.accept()
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = OutputStreamWriter(clientSocket.getOutputStream())

            val header = reader.readLine()
            assertEquals("ZAPYA_BOOST_STREAM", header)
            val transferId = reader.readLine()
            assertEquals("tx-12345", transferId)
            val offsetStr = reader.readLine()
            val offset = offsetStr?.toLongOrNull() ?: 0L
            assertEquals(0L, offset)

            writer.write("OK\n")
            writer.flush()

            val rawInput = clientSocket.getInputStream()
            val raf = RandomAccessFile(targetFile, "rw")
            raf.seek(offset)
            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var bytesTotal = offset
            while (true) {
                val read = rawInput.read(buffer)
                if (read == -1) break
                raf.write(buffer, 0, read)
                bytesTotal += read
            }
            raf.close()
            clientSocket.close()
            server.close()
            bytesTotal
        }

        val clientJob = async(Dispatchers.IO) {
            val socket = Socket()
            socket.connect(InetSocketAddress("127.0.0.1", port), 5000)
            socket.tcpNoDelay = true

            val writer = OutputStreamWriter(socket.getOutputStream())
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            writer.write("ZAPYA_BOOST_STREAM\n")
            writer.write("tx-12345\n")
            writer.write("0\n")
            writer.flush()

            val ack = reader.readLine()
            assertEquals("OK", ack)

            val inputStream = ByteArrayInputStream(originalData)
            val outputStream = socket.getOutputStream()
            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var sent = 0L
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break
                outputStream.write(buffer, 0, read)
                sent += read
            }
            outputStream.flush()
            socket.close()
            sent
        }

        val serverBytes = serverJob.await()
        val clientBytes = clientJob.await()

        assertEquals(testDataSize.toLong(), clientBytes)
        assertEquals(testDataSize.toLong(), serverBytes)
        assertEquals(testDataSize.toLong(), targetFile.length())
        assertArrayEquals(originalData, targetFile.readBytes())
    }

    @Test
    fun testTurboResumeFromBluetoothOffset() = runBlocking {
        // Simulates 2 MB file where first 512 KB was transferred over Bluetooth,
        // then Turbo Boost Wi-Fi kicked in and transferred remaining 1.5 MB seamlessly.
        val totalSize = 1024 * 1024 * 2 // 2 MB
        val fullData = ByteArray(totalSize).also { Random(99).nextBytes(it) }
        val resumeOffset = 1024L * 512L // 512 KB

        val targetPartFile = tempFolder.newFile("transfer.bin.part")
        // Write the initial 512 KB as if received over Bluetooth
        val rafInitial = RandomAccessFile(targetPartFile, "rw")
        rafInitial.write(fullData, 0, resumeOffset.toInt())
        rafInitial.close()
        assertEquals(resumeOffset, targetPartFile.length())

        val server = ServerSocket(0)
        val port = server.localPort

        val serverJob = async(Dispatchers.IO) {
            val clientSocket = server.accept()
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = OutputStreamWriter(clientSocket.getOutputStream())

            val header = reader.readLine()
            assertEquals("ZAPYA_BOOST_STREAM", header)
            val transferId = reader.readLine()
            assertEquals("resume-item-001", transferId)
            val offsetStr = reader.readLine()
            val offset = offsetStr.toLong()
            assertEquals(resumeOffset, offset)

            writer.write("OK\n")
            writer.flush()

            val rawInput = clientSocket.getInputStream()
            val raf = RandomAccessFile(targetPartFile, "rw")
            raf.seek(offset)
            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var bytesTotal = offset
            while (true) {
                val read = rawInput.read(buffer)
                if (read == -1) break
                raf.write(buffer, 0, read)
                bytesTotal += read
            }
            raf.close()
            clientSocket.close()
            server.close()
            bytesTotal
        }

        val clientJob = async(Dispatchers.IO) {
            val socket = Socket()
            socket.connect(InetSocketAddress("127.0.0.1", port), 5000)
            socket.tcpNoDelay = true

            val writer = OutputStreamWriter(socket.getOutputStream())
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            writer.write("ZAPYA_BOOST_STREAM\n")
            writer.write("resume-item-001\n")
            writer.write("$resumeOffset\n")
            writer.flush()

            val ack = reader.readLine()
            assertEquals("OK", ack)

            val inputStream = ByteArrayInputStream(fullData)
            val skipped = inputStream.skip(resumeOffset)
            assertEquals(resumeOffset, skipped)

            val outputStream = socket.getOutputStream()
            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var bytesSent = resumeOffset
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break
                outputStream.write(buffer, 0, read)
                bytesSent += read
            }
            outputStream.flush()
            socket.close()
            bytesSent
        }

        val serverTotal = serverJob.await()
        val clientTotal = clientJob.await()

        assertEquals(totalSize.toLong(), serverTotal)
        assertEquals(totalSize.toLong(), clientTotal)
        assertEquals(totalSize.toLong(), targetPartFile.length())

        val expectedSha = MessageDigest.getInstance("SHA-256").digest(fullData)
        val actualSha = MessageDigest.getInstance("SHA-256").digest(targetPartFile.readBytes())
        assertArrayEquals(expectedSha, actualSha)
    }

    @Test
    fun testInvalidHandshakeRejection() = runBlocking {
        val server = ServerSocket(0)
        val port = server.localPort
        val rejected = AtomicBoolean(false)

        val serverJob = async(Dispatchers.IO) {
            val clientSocket = server.accept()
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = OutputStreamWriter(clientSocket.getOutputStream())

            val header = reader.readLine()
            if (header != "ZAPYA_BOOST_STREAM") {
                writer.write("REJECTED: BAD_HEADER\n")
                writer.flush()
                rejected.set(true)
            }
            clientSocket.close()
            server.close()
        }

        val clientJob = async(Dispatchers.IO) {
            val socket = Socket()
            socket.connect(InetSocketAddress("127.0.0.1", port), 5000)
            val writer = OutputStreamWriter(socket.getOutputStream())
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            writer.write("MALICIOUS_PROTOCOL\n")
            writer.flush()

            val response = reader.readLine()
            assertEquals("REJECTED: BAD_HEADER", response)
            socket.close()
        }

        serverJob.await()
        clientJob.await()
        assertTrue(rejected.get())
    }

    @Test
    fun testFullTurboButtonClickToWifiTransferPipeline() = runBlocking {
        // 1. Arrange transfer item initially transferring on Bluetooth
        val itemId = "boost-button-tx-" + java.util.UUID.randomUUID().toString()
        val totalPayloadSize = 1024 * 1024 // 1 MB
        val fullData = ByteArray(totalPayloadSize).also { java.util.Random(77).nextBytes(it) }
        val initialBluetoothBytes = 256 * 1024L // 256 KB transferred initially
        val partFile = tempFolder.newFile("turbo_button_test.bin.part")

        // Pre-fill part file with initial Bluetooth bytes
        val rafInitial = RandomAccessFile(partFile, "rw")
        rafInitial.write(fullData, 0, initialBluetoothBytes.toInt())
        rafInitial.close()

        val mockUri = org.mockito.Mockito.mock(android.net.Uri::class.java)
        val initialItem = com.bastyoliva.wearoszapya.data.TransferItem(
            id = itemId,
            targetNodeId = "watch-node-active",
            uri = mockUri,
            fileName = "turbo_test_video.mp4",
            progress = 25,
            bytesTransferred = initialBluetoothBytes,
            totalBytes = totalPayloadSize.toLong(),
            status = com.bastyoliva.wearoszapya.data.TransferStatus.SENDING,
            mode = com.bastyoliva.wearoszapya.data.TransferMode.BLUETOOTH,
            isBoostActive = false
        )
        com.bastyoliva.wearoszapya.data.TransferRepository.addItem(initialItem)

        // 2. User clicks Turbo Boost button in UI -> triggerBoost(itemId)
        com.bastyoliva.wearoszapya.data.TransferRepository.triggerBoost(itemId)

        // 3. Service collects boost request
        val collectedBoostId = kotlinx.coroutines.withTimeout(5000) {
            com.bastyoliva.wearoszapya.data.TransferRepository.boostRequests.first()
        }
        assertEquals(itemId, collectedBoostId)

        // 4. Watch starts Turbo ServerSocket on port (simulated)
        val server = ServerSocket(0)
        val port = server.localPort

        // 5. Watch emits PATH_BOOST_READY payload: "$transferId|$ip|$port|$currentOffset"
        val boostReadyPayload = "$itemId|127.0.0.1|$port|$initialBluetoothBytes"
        val payloadParts = boostReadyPayload.split("|")
        val info = com.bastyoliva.wearoszapya.data.TurboConnectionInfo(
            transferId = payloadParts[0],
            ip = payloadParts[1],
            port = payloadParts[2].toInt(),
            confirmedOffset = payloadParts[3].toLong()
        )

        // 6. TransferStatusListener signals turbo ready
        com.bastyoliva.wearoszapya.data.TransferRepository.signalTurboReady(info)

        // 7. Watch Socket Server Job
        val serverJob = async(Dispatchers.IO) {
            val clientSocket = server.accept()
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = OutputStreamWriter(clientSocket.getOutputStream())

            val header = reader.readLine()
            assertEquals("ZAPYA_BOOST_STREAM", header)
            val reqId = reader.readLine()
            assertEquals(itemId, reqId)
            val offset = reader.readLine().toLong()
            assertEquals(initialBluetoothBytes, offset)

            writer.write("OK\n")
            writer.flush()

            val rawInput = clientSocket.getInputStream()
            val raf = RandomAccessFile(partFile, "rw")
            raf.seek(offset)
            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var bytesTotal = offset
            while (true) {
                val read = rawInput.read(buffer)
                if (read == -1) break
                raf.write(buffer, 0, read)
                bytesTotal += read
            }
            raf.close()
            clientSocket.close()
            server.close()
            bytesTotal
        }

        // 8. Mobile Client Socket Job
        val clientJob = async(Dispatchers.IO) {
            val socket = Socket()
            socket.connect(InetSocketAddress(info.ip, info.port), 5000)
            socket.tcpNoDelay = true

            val writer = OutputStreamWriter(socket.getOutputStream())
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            writer.write("ZAPYA_BOOST_STREAM\n")
            writer.write("${info.transferId}\n")
            writer.write("${info.confirmedOffset}\n")
            writer.flush()

            val ack = reader.readLine()
            assertEquals("OK", ack)

            // Switch UI state to Turbo Wi-Fi Active
            com.bastyoliva.wearoszapya.data.TransferRepository.updateItem(itemId) {
                it.copy(
                    mode = com.bastyoliva.wearoszapya.data.TransferMode.TURBO_WIFI,
                    isBoostActive = true,
                    speedText = "⚡ Turbo Active"
                )
            }

            val inputStream = ByteArrayInputStream(fullData)
            inputStream.skip(info.confirmedOffset)

            val outputStream = socket.getOutputStream()
            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var sent = info.confirmedOffset
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) break
                outputStream.write(buffer, 0, read)
                sent += read
            }
            outputStream.flush()
            socket.close()

            // Mark complete
            com.bastyoliva.wearoszapya.data.TransferRepository.updateItem(itemId) {
                it.copy(
                    progress = 100,
                    bytesTransferred = totalPayloadSize.toLong(),
                    status = com.bastyoliva.wearoszapya.data.TransferStatus.SUCCESS,
                    speedText = "Complete"
                )
            }
            sent
        }

        val totalServer = serverJob.await()
        val totalClient = clientJob.await()

        assertEquals(totalPayloadSize.toLong(), totalServer)
        assertEquals(totalPayloadSize.toLong(), totalClient)
        assertEquals(totalPayloadSize.toLong(), partFile.length())

        // 9. Verify UI state updated to SUCCESS
        val finalItem = com.bastyoliva.wearoszapya.data.TransferRepository.queue.first { it.id == itemId }
        assertEquals(com.bastyoliva.wearoszapya.data.TransferMode.TURBO_WIFI, finalItem.mode)
        assertTrue(finalItem.isBoostActive)
        assertEquals(com.bastyoliva.wearoszapya.data.TransferStatus.SUCCESS, finalItem.status)
        assertEquals(100, finalItem.progress)

        // 10. Verify exact SHA-256 match
        val expectedDigest = MessageDigest.getInstance("SHA-256").digest(fullData)
        val actualDigest = MessageDigest.getInstance("SHA-256").digest(partFile.readBytes())
        assertArrayEquals(expectedDigest, actualDigest)

        // Clean up
        com.bastyoliva.wearoszapya.data.TransferRepository.removeItem(itemId)
    }
}
