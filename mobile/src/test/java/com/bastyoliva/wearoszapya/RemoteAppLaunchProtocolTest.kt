package com.bastyoliva.wearoszapya

import com.bastyoliva.wearoszapya.data.TurboConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class RemoteAppLaunchProtocolTest {

    @Test
    fun testDeepLinkUriSchemeStructure() {
        val uri = URI.create(TurboConstants.URI_SCHEME_OPEN)
        assertEquals("wearoszapya", uri.scheme)
        assertEquals("open", uri.host)
    }

    @Test
    fun testMessagePathDispatching() {
        val handledPaths = mutableListOf<String>()

        fun simulateIncomingMessage(path: String) {
            when (path) {
                TurboConstants.PATH_OPEN_APP -> handledPaths.add("LAUNCH_APP")
                TurboConstants.PATH_OPEN_WIFI_SETTINGS -> handledPaths.add("OPEN_WIFI")
                TurboConstants.PATH_OPEN_HOTSPOT_SETTINGS -> handledPaths.add("OPEN_HOTSPOT")
                TurboConstants.PATH_BOOST_WAKE_WIFI -> handledPaths.add("WAKE_WIFI")
                TurboConstants.PATH_BOOST_STATUS -> handledPaths.add("STATUS_UPDATE")
            }
        }

        simulateIncomingMessage("/zapya/open-app")
        simulateIncomingMessage("/zapya/open-wifi-settings")
        simulateIncomingMessage("/zapya/open-hotspot-settings")
        simulateIncomingMessage("/zapya/boost/wake-wifi")
        simulateIncomingMessage("/zapya/boost/status")

        assertEquals(
            listOf("LAUNCH_APP", "OPEN_WIFI", "OPEN_HOTSPOT", "WAKE_WIFI", "STATUS_UPDATE"),
            handledPaths
        )
    }

    @Test
    fun testStatusMessagePayloadParsing() {
        val readyPayload = "TURBO_READY|192.168.43.155|8988|1048576"
        val parts = readyPayload.split("|")

        assertEquals(4, parts.size)
        assertEquals("TURBO_READY", parts[0])
        assertEquals("192.168.43.155", parts[1])
        assertEquals(8988, parts[2].toInt())
        assertEquals(1048576L, parts[3].toLong())

        val noWifiPayload = "NO_WIFI"
        assertTrue(noWifiPayload.startsWith("NO_WIFI"))
    }
}
