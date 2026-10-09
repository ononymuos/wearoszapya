package com.bastyoliva.wearoszapya.data

object TurboConstants {
    const val CAPABILITY_NAME = "wear_os_zapya_app"
    const val TURBO_PORT = 8988
    
    // Bluetooth message paths
    const val PATH_BOOST_REQUEST = "/zapya/boost/request"
    const val PATH_BOOST_READY = "/zapya/boost/ready"
    const val PATH_BOOST_FALLBACK = "/zapya/boost/fallback"
    const val PATH_BOOST_STATUS = "/zapya/boost/status"
    const val PATH_BOOST_WAKE_WIFI = "/zapya/boost/wake-wifi"
    const val PATH_TRANSFER_STATUS = "/zapya/transfer-status"
    const val PATH_OPEN_APP = "/zapya/open-app"
    const val PATH_OPEN_WIFI_SETTINGS = "/zapya/open-wifi-settings"
    const val PATH_OPEN_HOTSPOT_SETTINGS = "/zapya/open-hotspot-settings"
    
    // Deep link scheme
    const val URI_SCHEME_OPEN = "wearoszapya://open"
    
    // Bluetooth stream channel prefix: /zapya-transfer/{transferId}/{offset}/{totalSize}/{fileName}
    const val CHANNEL_TRANSFER_PREFIX = "/zapya-transfer/"
    
    // Buffer sizes
    const val BUFFER_SIZE_BLUETOOTH = 32768   // 32 KB
    const val BUFFER_SIZE_TURBO = 65536       // 64 KB
}
