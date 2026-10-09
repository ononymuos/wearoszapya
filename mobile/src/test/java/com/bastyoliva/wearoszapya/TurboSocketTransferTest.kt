package com.bastyoliva.wearoszapya

import com.bastyoliva.wearoszapya.data.TurboConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
}
