package com.bastyoliva.wearoszapya

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bastyoliva.wearoszapya.data.TurboConstants
import com.bastyoliva.wearoszapya.turbo.TurboNetworkManager
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class FileReceiverService : WearableListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val activeTransfers = AtomicInteger(0)

    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val powerManager by lazy {
        getSystemService(POWER_SERVICE) as PowerManager
    }

    private lateinit var turboNetworkManager: TurboNetworkManager

    // Tracks confirmed bytes written per transferId
    private val transferOffsets = ConcurrentHashMap<String, Long>()
    private val transferNodes = ConcurrentHashMap<String, String>()
    private val turboActiveTransfers = ConcurrentHashMap<String, Boolean>()

    companion object {
        private const val TAG = "FileReceiverService"
        private const val CHANNEL_ID = "file_receiver_channel"
        private const val NOTIFICATION_ID = 1001
        private const val WAKE_LOCK_TIMEOUT_MS = 20 * 60 * 1000L // 20 minutes
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        turboNetworkManager = TurboNetworkManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(fileName: String, progress: Int, isTurbo: Boolean = false, speed: String = ""): Notification {
        val title = if (isTurbo) {
            "⚡ Turbo Boost (Wi-Fi): $fileName"
        } else {
            getString(R.string.notification_receiving_title)
        }
        
        val contentText = buildString {
            if (progress >= 0) {
                append(getString(R.string.notification_receiving_desc, fileName, progress))
            } else {
                append(fileName)
            }
            if (speed.isNotEmpty()) {
                append(" • ").append(speed)
            }
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (progress >= 0) {
            builder.setProgress(100, progress, false)
        } else {
            builder.setProgress(0, 0, true)
        }

        return builder.build()
    }

    private fun startForegroundServiceIfNeeded(fileName: String) {
        val notification = buildNotification(fileName, 0)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
        }
    }

    private fun updateProgressNotification(fileName: String, progress: Int, isTurbo: Boolean = false, speed: String = "") {
        val notification = buildNotification(fileName, progress, isTurbo, speed)
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update notification: ${e.message}")
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        when (messageEvent.path) {
            TurboConstants.PATH_OPEN_APP, "/open-app" -> {
                Log.d(TAG, "Received open-app request from phone")
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                if (launchIntent != null) {
                    try {
                        startActivity(launchIntent)
                    } catch (e: Exception) {
                        Log.w(TAG, "Direct launch failed: ${e.message}")
                    }
                }
                showRemoteLaunchNotification()
            }

            TurboConstants.PATH_OPEN_WIFI_SETTINGS -> {
                Log.d(TAG, "Received open-wifi-settings request from phone")
                try {
                    val wifiIntent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(wifiIntent)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to launch wifi settings: ${e.message}")
                }
            }

            TurboConstants.PATH_BOOST_WAKE_WIFI -> {
                Log.d(TAG, "Received wake-wifi request from phone")
                turboNetworkManager.requestHighSpeedWifi(
                    onWifiReady = { ip ->
                        Log.d(TAG, "Wi-Fi woken up with IP: $ip")
                        scope.launch {
                            try {
                                Wearable.getMessageClient(this@FileReceiverService)
                                    .sendMessage(messageEvent.sourceNodeId, TurboConstants.PATH_BOOST_STATUS, "WIFI_READY|$ip".toByteArray())
                                    .await()
                            } catch (_: Exception) {}
                        }
                    },
                    onWifiLost = {},
                    onTimeout = {
                        scope.launch {
                            try {
                                Wearable.getMessageClient(this@FileReceiverService)
                                    .sendMessage(messageEvent.sourceNodeId, TurboConstants.PATH_BOOST_STATUS, "NO_WIFI".toByteArray())
                                    .await()
                            } catch (_: Exception) {}
                        }
                    }
                )
            }

            TurboConstants.PATH_BOOST_REQUEST -> {
                val transferId = String(messageEvent.data)
                Log.d(TAG, "Received boost request for transfer: $transferId from node: ${messageEvent.sourceNodeId}")
                transferNodes[transferId] = messageEvent.sourceNodeId
                enableTurboBoost(transferId, messageEvent.sourceNodeId)
            }
        }
    }

    private fun showRemoteLaunchNotification() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            201,
            launchIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Phone requested to open watch app")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)

        try {
            notificationManager.notify(201, builder.build())
        } catch (_: Exception) {}
    }

    private fun enableTurboBoost(transferId: String, nodeId: String) {
        turboNetworkManager.requestHighSpeedWifi(
            onWifiReady = { ip ->
                scope.launch {
                    val currentOffset = transferOffsets[transferId] ?: 0L
                    val payload = "$transferId|$ip|${TurboConstants.TURBO_PORT}|$currentOffset"
                    try {
                        Wearable.getMessageClient(this@FileReceiverService)
                            .sendMessage(nodeId, TurboConstants.PATH_BOOST_READY, payload.toByteArray())
                            .await()
                        Log.d(TAG, "Sent turbo ready message: $payload")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send turbo ready: ${e.message}")
                    }
                }
            },
            onWifiLost = {
                Log.w(TAG, "Turbo Wi-Fi connection lost, falling back to Bluetooth")
                scope.launch {
                    val currentOffset = transferOffsets[transferId] ?: 0L
                    val payload = "$transferId|$currentOffset"
                    try {
                        Wearable.getMessageClient(this@FileReceiverService)
                            .sendMessage(nodeId, TurboConstants.PATH_BOOST_FALLBACK, payload.toByteArray())
                            .await()
                    } catch (_: Exception) {}
                }
            },
            onTimeout = {
                Log.w(TAG, "Turbo Wi-Fi timeout for transfer $transferId")
                scope.launch {
                    try {
                        Wearable.getMessageClient(this@FileReceiverService)
                            .sendMessage(nodeId, TurboConstants.PATH_BOOST_STATUS, "NO_WIFI".toByteArray())
                            .await()
                    } catch (_: Exception) {}
                }
            }
        )
    }

    override fun onChannelOpened(channel: ChannelClient.Channel) {
        Log.d(TAG, "Channel opened: ${channel.path}")
        val path = channel.path

        var transferId = ""
        var initialOffset = 0L
        var expectedSize = -1L
        var fileName = ""

        if (path.startsWith(TurboConstants.CHANNEL_TRANSFER_PREFIX)) {
            // Format: /zapya-transfer/{transferId}/{offset}/{totalSize}/{fileName}
            val segments = path.substringAfter(TurboConstants.CHANNEL_TRANSFER_PREFIX).split("/", limit = 4)
            if (segments.size >= 4) {
                transferId = segments[0]
                initialOffset = segments[1].toLongOrNull() ?: 0L
                expectedSize = segments[2].toLongOrNull() ?: -1L
                fileName = Uri.decode(segments[3])
            }
        } else if (path.startsWith("/file-transfer/")) {
            val pathData = path.substringAfter("/file-transfer/")
            if (pathData.contains("/")) {
                expectedSize = pathData.substringBefore("/").toLongOrNull() ?: -1L
                fileName = Uri.decode(pathData.substringAfter("/"))
            } else {
                fileName = Uri.decode(pathData)
            }
            transferId = fileName
        } else {
            return
        }

        val nodeId = channel.nodeId
        transferNodes[transferId] = nodeId
        transferOffsets[transferId] = initialOffset

        try {
            val intent = Intent(this, FileReceiverService::class.java)
            startForegroundService(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start service intent: ${e.message}")
        }

        val currentActive = activeTransfers.incrementAndGet()
        if (currentActive == 1) {
            startForegroundServiceIfNeeded(fileName)
        } else {
            updateProgressNotification(fileName, 0)
        }

        // Proactively request high-speed Wi-Fi so Boost is ready instantly
        enableTurboBoost(transferId, nodeId)

        scope.launch {
            receiveFileStream(channel, transferId, fileName, nodeId, expectedSize, initialOffset)
        }
    }

    private suspend fun receiveFileStream(
        channel: ChannelClient.Channel,
        transferId: String,
        fileName: String,
        nodeId: String,
        expectedSize: Long,
        initialOffset: Long
    ) {
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WearOsZapya:FileReceiverWakeLock"
        )
        try {
            wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock: ${e.message}")
        }

        val channelClient = Wearable.getChannelClient(this)
        val receivedDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Received")
        if (!receivedDir.exists()) {
            receivedDir.mkdirs()
        }

        val finalFile = File(receivedDir, fileName)
        val partFile = File(receivedDir, "$fileName.part")

        Log.d(TAG, "Receiving file: $fileName (offset=$initialOffset, total=$expectedSize) to ${partFile.absolutePath}")

        try {
            // Check if Turbo Wi-Fi takes over
            val turboJob = scope.launch {
                turboNetworkManager.startTurboReceiver(
                    targetTransferId = transferId,
                    partFile = partFile,
                    totalSize = expectedSize,
                    initialOffset = transferOffsets[transferId] ?: initialOffset,
                    onProgress = { bytes, speed ->
                        turboActiveTransfers[transferId] = true
                        transferOffsets[transferId] = bytes
                        val progress = if (expectedSize > 0) (bytes * 100 / expectedSize).toInt() else -1
                        updateProgressNotification(fileName, progress, isTurbo = true, speed = speed)
                    },
                    onComplete = { bytes ->
                        transferOffsets[transferId] = bytes
                    },
                    onError = { e ->
                        turboActiveTransfers[transferId] = false
                        Log.w(TAG, "Turbo transfer ended or falling back: ${e.message}")
                    }
                )
            }

            // Normal Bluetooth channel reception (runs until complete or Turbo supersedes it)
            val inputStream = channelClient.getInputStream(channel).await()
            var bytesWritten = transferOffsets[transferId] ?: initialOffset
            var lastNotificationTime = 0L

            withContext(Dispatchers.IO) {
                val raf = RandomAccessFile(partFile, "rw")
                raf.seek(bytesWritten)
                inputStream.use { input ->
                    val buffer = ByteArray(TurboConstants.BUFFER_SIZE_BLUETOOTH)
                    var read = input.read(buffer)
                    while (read != -1) {
                        // If Turbo mode took over and already made progress past this point, yield
                        val currentTurboOffset = transferOffsets[transferId] ?: bytesWritten
                        if (turboActiveTransfers[transferId] == true && currentTurboOffset > bytesWritten) {
                            Log.d(TAG, "Bluetooth channel superseded by Turbo Wi-Fi at offset $currentTurboOffset")
                            break
                        }

                        raf.write(buffer, 0, read)
                        bytesWritten += read
                        transferOffsets[transferId] = bytesWritten

                        val now = System.currentTimeMillis()
                        if (now - lastNotificationTime > 500) {
                            val progress = if (expectedSize > 0) (bytesWritten * 100 / expectedSize).toInt() else -1
                            updateProgressNotification(fileName, progress, isTurbo = false, speed = "~30 KB/s")
                            lastNotificationTime = now
                        }

                        if (expectedSize > 0 && bytesWritten >= expectedSize) {
                            break
                        }
                        read = input.read(buffer)
                    }
                    raf.close()
                }
            }

            // Wait for Turbo job if active
            if (turboActiveTransfers[transferId] == true) {
                turboJob.join()
            }

            val finalBytes = transferOffsets[transferId] ?: bytesWritten
            if (expectedSize > 0 && finalBytes < expectedSize) {
                Log.w(TAG, "Transfer paused or incomplete at $finalBytes of $expectedSize bytes")
                return
            }

            // Atomically rename .part to finalFile
            if (partFile.exists()) {
                if (finalFile.exists()) finalFile.delete()
                partFile.renameTo(finalFile)
            }

            Log.d(TAG, "File transfer completed successfully: ${finalFile.absolutePath}")

            try {
                Wearable.getMessageClient(this)
                    .sendMessage(nodeId, TurboConstants.PATH_TRANSFER_STATUS, "success:$fileName".toByteArray())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send success status: ${e.message}")
            }

        } catch (e: CancellationException) {
            Log.w(TAG, "Receive cancelled for $fileName", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error receiving file: ${e.message}", e)
            try {
                Wearable.getMessageClient(this)
                    .sendMessage(nodeId, TurboConstants.PATH_TRANSFER_STATUS, "error:$fileName".toByteArray())
                    .await()
            } catch (_: Exception) {}
        } finally {
            if (wakeLock.isHeld) {
                try {
                    wakeLock.release()
                } catch (_: Exception) {}
            }

            try {
                channelClient.close(channel).await()
            } catch (_: Exception) {}

            turboNetworkManager.release()

            if (activeTransfers.decrementAndGet() <= 0) {
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (_: Exception) {}
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        turboNetworkManager.release()
        job.cancel()
    }
}