package com.bastyoliva.wearoszapya.data

object TurboConstants {
    const val CAPABILITY_NAME = "wear_os_zapya_app"
    const val TURBO_PORT = 8988
    
    // Bluetooth message paths
    const val PATH_BOOST_REQUEST = "/zapya/boost/request"
    const val PATH_BOOST_READY = "/zapya/boost/ready"
    const val PATH_BOOST_FALLBACK = "/zapya/boost/fallback"
    const val PATH_TRANSFER_STATUS = "/zapya/transfer-status"
    
    // Bluetooth stream channel prefix: /zapya-transfer/{transferId}/{offset}/{totalSize}/{fileName}
    const val CHANNEL_TRANSFER_PREFIX = "/zapya-transfer/"
    
    // Buffer sizes
    const val BUFFER_SIZE_BLUETOOTH = 32768   // 32 KB
    const val BUFFER_SIZE_TURBO = 65536       // 64 KB
}
