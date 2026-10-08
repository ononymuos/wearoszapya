package com.bastyoliva.wearoszapya.turbo

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log
import com.bastyoliva.wearoszapya.data.TurboConstants
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.RandomAccessFile
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class TurboNetworkManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var wifiLock: WifiManager.WifiLock? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var serverSocket: ServerSocket? = null
    private var activeClientSocket: Socket? = null
    private val isRunning = AtomicBoolean(false)

    companion object {
        private const val TAG = "TurboNetworkManager"
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining local IP: ${e.message}")
        }
        return null
    }

    fun requestHighSpeedWifi(
        onWifiReady: (ip: String) -> Unit,
        onWifiLost: () -> Unit
    ) {
        try {
            @Suppress("DEPRECATION")
            wifiLock = wifiManager.createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                "WearOsZapya:TurboWifiLock"
            ).apply {
                setReferenceCounted(false)
                acquire()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wifi lock: ${e.message}")
        }

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "High-bandwidth Wi-Fi network available: $network")
                try {
                    connectivityManager.bindProcessToNetwork(network)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to bind process to Wi-Fi: ${e.message}")
                }
                
                // Allow a brief moment for DHCP / IP assignment
                CoroutineScope(Dispatchers.IO).launch {
                    var ip: String? = null
                    for (i in 0..10) {
                        ip = getLocalIpAddress()
                        if (ip != null) break
                        delay(200)
                    }
                    if (ip != null) {
                        Log.d(TAG, "Wi-Fi Ready with IP: $ip")
                        withContext(Dispatchers.Main) {
                            onWifiReady(ip)
                        }
                    } else {
                        Log.w(TAG, "Wi-Fi connected but could not find IPv4 address")
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "Wi-Fi network lost: $network")
                onWifiLost()
            }
        }

        try {
            connectivityManager.requestNetwork(request, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request Wi-Fi network: ${e.message}")
        }
    }

    suspend fun startTurboReceiver(
        targetTransferId: String,
        partFile: File,
        totalSize: Long,
        initialOffset: Long,
        onProgress: (bytesReceived: Long, speed: String) -> Unit,
        onComplete: (bytesReceived: Long) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        isRunning.set(true)
        try {
            serverSocket = ServerSocket(TurboConstants.TURBO_PORT).apply {
                reuseAddress = true
                soTimeout = 45000 // 45 seconds accept timeout
            }
            Log.d(TAG, "Turbo ServerSocket listening on port ${TurboConstants.TURBO_PORT}")

            val clientSocket = serverSocket?.accept() ?: return@withContext
            activeClientSocket = clientSocket
            Log.d(TAG, "Client connected from ${clientSocket.inetAddress.hostAddress}")

            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = OutputStreamWriter(clientSocket.getOutputStream())

            val header = reader.readLine()
            if (header == null || !header.startsWith("ZAPYA_BOOST_STREAM")) {
                throw Exception("Invalid handshake header: $header")
            }
            val transferId = reader.readLine()
            val offsetStr = reader.readLine()
            val offset = offsetStr?.toLongOrNull() ?: initialOffset

            Log.d(TAG, "Handshake verified for transferId=$transferId starting from offset=$offset")
            writer.write("OK\n")
            writer.flush()

            val rawInput = clientSocket.getInputStream()
            val raf = RandomAccessFile(partFile, "rw")
            raf.seek(offset)

            val buffer = ByteArray(TurboConstants.BUFFER_SIZE_TURBO)
            var bytesTotal = offset
            var lastTime = System.currentTimeMillis()
            var bytesSinceLastTime = 0L

            while (isRunning.get()) {
                val read = rawInput.read(buffer)
                if (read == -1) break
                raf.write(buffer, 0, read)
                bytesTotal += read
                bytesSinceLastTime += read

                val now = System.currentTimeMillis()
                val elapsed = now - lastTime
                if (elapsed >= 400) {
                    val speedMBs = (bytesSinceLastTime * 1000.0) / (elapsed * 1024.0 * 1024.0)
                    val speedText = String.format("%.1f MB/s", speedMBs)
                    onProgress(bytesTotal, speedText)
                    lastTime = now
                    bytesSinceLastTime = 0L
                }

                if (totalSize > 0 && bytesTotal >= totalSize) {
                    break
                }
            }

            raf.close()
            Log.d(TAG, "Finished Turbo receive: $bytesTotal of $totalSize bytes")
            if (totalSize > 0 && bytesTotal < totalSize) {
                throw Exception("Incomplete Turbo transfer: received $bytesTotal of $totalSize")
            }
            onComplete(bytesTotal)

        } catch (e: Exception) {
            Log.e(TAG, "Turbo receiver error: ${e.message}", e)
            onError(e)
        } finally {
            closeSockets()
        }
    }

    fun closeSockets() {
        try {
            activeClientSocket?.close()
        } catch (_: Exception) {}
        activeClientSocket = null

        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
    }

    fun release() {
        isRunning.set(false)
        closeSockets()
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
            networkCallback = null
        }
        if (wifiLock?.isHeld == true) {
            try {
                wifiLock?.release()
            } catch (_: Exception) {}
        }
        wifiLock = null
    }
}
