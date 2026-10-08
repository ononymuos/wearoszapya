package com.bastyoliva.wearoszapya

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.bastyoliva.wearoszapya.data.TransferItem
import com.bastyoliva.wearoszapya.data.TransferMode
import com.bastyoliva.wearoszapya.data.TransferRepository
import com.bastyoliva.wearoszapya.data.TransferStatus
import com.bastyoliva.wearoszapya.data.TurboConnectionInfo
import com.bastyoliva.wearoszapya.data.TurboConstants
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.milliseconds

class FileTransferService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var notificationHelper: NotificationHelper
    private var isProcessing = false

    companion object {
        private const val TAG = "FileTransferService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)

        // Listen for user boost trigger requests
        scope.launch {
            TransferRepository.boostRequests.collect { transferId ->
                val activeItem = TransferRepository.queue.firstOrNull { it.id == transferId && it.status == TransferStatus.SENDING }
                if (activeItem != null) {
                    try {
                        Log.d(TAG, "Sending boost request for transfer: $transferId to ${activeItem.targetNodeId}")
                        Wearable.getMessageClient(this@FileTransferService)
                            .sendMessage(
                                activeItem.targetNodeId,
                                TurboConstants.PATH_BOOST_REQUEST,
                                transferId.toByteArray()
                            ).await()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send boost request: ${e.message}")
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "CANCEL_TRANSFER" -> {
                val id = intent.getStringExtra("item_id")
                if (id != null) {
                    TransferRepository.removeItem(id)
                }
                return START_NOT_STICKY
            }
            "TRIGGER_BOOST" -> {
                val id = intent.getStringExtra("item_id")
                if (id != null) {
                    TransferRepository.triggerBoost(id)
                }
                return START_NOT_STICKY
            }
            "ADD_TRANSFER" -> {
                @Suppress("DEPRECATION") val uri = intent.getParcelableExtra<Uri>("file_uri")
                val fileName = intent.getStringExtra("file_name") ?: getString(R.string.file_default_name)
                val nodeId = intent.getStringExtra("target_node_id")
                if (uri != null && nodeId != null) {
                    val newItem = TransferItem(
                        id = UUID.randomUUID().toString(),
                        targetNodeId = nodeId,
                        uri = uri,
                        fileName = fileName
                    )
                    TransferRepository.addItem(newItem)
                    startProcessing()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startProcessing() {
        if (isProcessing) return
        isProcessing = true
        
        val notification = notificationHelper.getNotification(getString(R.string.notification_file_queue), TransferStatus.SENDING)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        scope.launch {
            while (isActive) {
                val nextItem = TransferRepository.queue.firstOrNull { it.status == TransferStatus.PENDING }
                if (nextItem == null) {
                    isProcessing = false
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                    break
                }

                try {
                    processItem(nextItem)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing item ${nextItem.id}", e)
                    TransferRepository.updateItem(nextItem.id) { it.copy(status = TransferStatus.ERROR) }
                    notificationHelper.showTransferNotification(nextItem.fileName, TransferStatus.ERROR)
                }
            }
        }
    }

    private suspend fun processItem(item: TransferItem) {
        if (TransferRepository.queue.none { it.id == item.id }) return

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WearOsZapya:FileTransferWakeLock"
        )
        try {
            wakeLock.acquire(20 * 60 * 1000L)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock: ${e.message}")
        }

        val totalSize = try {
            contentResolver.query(item.uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) cursor.getLong(sizeIndex) else -1L
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }

        TransferRepository.updateItem(item.id) {
            it.copy(
                status = TransferStatus.SENDING,
                totalBytes = totalSize,
                mode = TransferMode.BLUETOOTH,
                speedText = "~30 KB/s"
            )
        }

        val confirmedOffset = AtomicLong(0L)
        val switchToTurbo = AtomicBoolean(false)
        var turboConnectionInfo: TurboConnectionInfo? = null

        // Collect turbo events for this item
        val turboJob = scope.launch {
            TransferRepository.turboEvents.collectLatest { info ->
                if (info.transferId == item.id) {
                    Log.d(TAG, "Turbo ready received for ${item.id}: ip=${info.ip}, port=${info.port}, offset=${info.confirmedOffset}")
                    turboConnectionInfo = info
                    switchToTurbo.set(true)
                }
            }
        }

        try {
            while (confirmedOffset.get() < totalSize || totalSize <= 0) {
                if (TransferRepository.queue.none { it.id == item.id }) {
                    throw CancellationException(getString(R.string.error_canceled))
                }

                if (switchToTurbo.get() && turboConnectionInfo != null) {
                    val info = turboConnectionInfo!!
                    val turboSuccess = runTurboTransfer(item, info, totalSize, confirmedOffset)
                    if (turboSuccess) {
                        break
                    } else {
                        Log.w(TAG, "Turbo socket failed or closed. Resuming via Bluetooth from ${confirmedOffset.get()}")
                        switchToTurbo.set(false)
                        turboConnectionInfo = null
                    }
                } else {
                    val currentOffset = confirmedOffset.get()
                    val btSuccess = runBluetoothTransfer(item, currentOffset, totalSize, confirmedOffset, switchToTurbo)
                    if (btSuccess) {
                        break
                    } else if (switchToTurbo.get()) {
                        // Switch immediately to turbo
                        continue
                    } else {
                        // Retrying after a short delay or broken stream
                        delay(500)
                    }
                }
            }

            TransferRepository.updateItem(item.id) {
                it.copy(
                    progress = 100,
                    bytesTransferred = totalSize,
                    status = TransferStatus.SUCCESS,
                    speedText = "Complete"
                )
            }
            notificationHelper.showTransferNotification(item.fileName, TransferStatus.SUCCESS)

        } finally {
            turboJob.cancel()
            if (wakeLock.isHeld) {
                try {
                    wakeLock.release()
                } catch (_: Exception) {}
            }
        }
    }

    private suspend fun runTurboTransfer(
        item: TransferItem,
        info: TurboConnectionInfo,
        totalSize: Long,
        confirmedOffset: AtomicLong
    ): Boolean = withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            Log.d(TAG, "Connecting to Turbo Wi-Fi Socket at ${info.ip}:${info.port}...")
            socket.connect(InetSocketAddress(info.ip, info.port), 6000)
            socket.tcpNoDelay = true

            val writer = OutputStreamWriter(socket.getOutputStream())
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

            val startOffset = maxOf(info.confirmedOffset, confirmedOffset.get())

            // Send Turbo Handshake
            writer.write("ZAPYA_BOOST_STREAM\n")
            writer.write("${item.id}\n")
            writer.write("$startOffset\n")
            writer.flush()

            val ack = reader.readLine()
            if (ack != "OK") {
                Log.e(TAG, "Turbo handshake rejected: $ack")
                return@withContext false
            }

            TransferRepository.updateItem(item.id) {
                it.copy(
                    mode = TransferMode.TURBO_WIFI,
                    isBoostActive = true,
                    speedText = "⚡ Turbo Active"
                )
            }

            val inputStream = contentResolver.openInputStream(item.uri) ?: return@withContext false
            val outputStream = socket.getOutputStream()

            // Skip to startOffset
            if (startOffset > 0) {
                var skipped = 0L
                while (skipped < startOffset) {
                    val s = inputStream.skip(startOffset - skipped)
                    if (s <= 0) break
                    skipped += s
                }
            }

            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var bytesWritten = startOffset
            var lastUpdate = System.currentTimeMillis()
            var bytesSinceLast = 0L

            inputStream.use { input ->
                outputStream.use { out ->
                    var read = input.read(buffer)
                    while (read != -1) {
                        if (TransferRepository.queue.none { it.id == item.id }) {
                            throw CancellationException(getString(R.string.error_canceled))
                        }

                        out.write(buffer, 0, read)
                        bytesWritten += read
                        bytesSinceLast += read
                        confirmedOffset.set(bytesWritten)

                        val now = System.currentTimeMillis()
                        val elapsed = now - lastUpdate
                        if (elapsed >= 300) {
                            val speedMBs = (bytesSinceLast * 1000.0) / (elapsed * 1024.0 * 1024.0)
                            val speedText = String.format("⚡ %.1f MB/s", speedMBs)
                            val progress = if (totalSize > 0) (bytesWritten * 100 / totalSize).toInt() else 0

                            TransferRepository.updateItem(item.id) {
                                it.copy(
                                    progress = progress,
                                    bytesTransferred = bytesWritten,
                                    speedText = speedText
                                )
                            }
                            notificationHelper.showTransferNotification(item.fileName, TransferStatus.SENDING, progress)

                            lastUpdate = now
                            bytesSinceLast = 0L
                        }

                        if (totalSize > 0 && bytesWritten >= totalSize) {
                            break
                        }
                        read = input.read(buffer)
                    }
                    out.flush()
                }
            }

            Log.d(TAG, "Turbo transfer finished with $bytesWritten bytes")
            return@withContext (totalSize <= 0 || bytesWritten >= totalSize)

        } catch (e: Exception) {
            Log.w(TAG, "Turbo socket streaming ended: ${e.message}")
            return@withContext false
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private suspend fun runBluetoothTransfer(
        item: TransferItem,
        startOffset: Long,
        totalSize: Long,
        confirmedOffset: AtomicLong,
        switchToTurbo: AtomicBoolean
    ): Boolean = withContext(Dispatchers.IO) {
        val channelClient = Wearable.getChannelClient(this@FileTransferService)
        val channelPath = "${TurboConstants.CHANNEL_TRANSFER_PREFIX}${item.id}/$startOffset/$totalSize/${Uri.encode(item.fileName)}"
        
        Log.d(TAG, "Opening Bluetooth channel on $channelPath from offset=$startOffset")
        val channel = try {
            channelClient.openChannel(item.targetNodeId, channelPath).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Bluetooth channel: ${e.message}")
            return@withContext false
        }

        try {
            delay(300.milliseconds)
            val outputStream = channelClient.getOutputStream(channel).await()
            val inputStream = contentResolver.openInputStream(item.uri) ?: throw Exception(getString(R.string.error_input_stream))

            if (startOffset > 0) {
                var skipped = 0L
                while (skipped < startOffset) {
                    val s = inputStream.skip(startOffset - skipped)
                    if (s <= 0) break
                    skipped += s
                }
            }

            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_BLUETOOTH)
            var bytesWritten = startOffset
            var lastUpdate = System.currentTimeMillis()
            var bytesSinceLast = 0L

            inputStream.use { input ->
                outputStream.use { out ->
                    var read = input.read(buffer)
                    while (read != -1) {
                        if (TransferRepository.queue.none { it.id == item.id }) {
                            throw CancellationException(getString(R.string.error_canceled))
                        }

                        // If user switched to Turbo mode, pause Bluetooth cleanly
                        if (switchToTurbo.get()) {
                            Log.d(TAG, "Bluetooth transfer yielding to Turbo at offset=$bytesWritten")
                            return@withContext false
                        }

                        out.write(buffer, 0, read)
                        bytesWritten += read
                        bytesSinceLast += read
                        confirmedOffset.set(bytesWritten)

                        val now = System.currentTimeMillis()
                        val elapsed = now - lastUpdate
                        if (now - lastUpdate > 300) {
                            val speedKBs = (bytesSinceLast * 1000.0) / (elapsed * 1024.0)
                            val speedText = String.format("%.0f KB/s", speedKBs)
                            val progress = if (totalSize > 0) (bytesWritten * 100 / totalSize).toInt() else 0

                            TransferRepository.updateItem(item.id) {
                                it.copy(
                                    progress = progress,
                                    bytesTransferred = bytesWritten,
                                    mode = TransferMode.BLUETOOTH,
                                    speedText = speedText
                                )
                            }
                            notificationHelper.showTransferNotification(item.fileName, TransferStatus.SENDING, progress)
                            lastUpdate = now
                            bytesSinceLast = 0L
                        }

                        if (totalSize > 0 && bytesWritten >= totalSize) {
                            break
                        }
                        read = input.read(buffer)
                    }
                    out.flush()
                }
            }

            return@withContext (totalSize <= 0 || bytesWritten >= totalSize)

        } catch (e: Exception) {
            Log.w(TAG, "Bluetooth channel transfer ended: ${e.message}")
            return@withContext false
        } finally {
            try {
                channelClient.close(channel).await()
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
