package io.homeassistant.companion.android.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.homeassistant.companion.android.common.data.servers.ServerManager
import io.homeassistant.companion.android.common.data.wifi.WifiHelper
import io.homeassistant.companion.android.onboarding.login.HassioUserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MSHAutoWifiManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverManager: ServerManager,
    private val wifiHelper: WifiHelper
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Automatically detect and add current WiFi SSID to the server's internal SSIDs
     * @param serverId The ID of the server to update
     * @return True if SSID was added successfully, false otherwise
     */
    fun autoAddCurrentWifi(serverId: Int) {
        scope.launch {
            try {
                if (!wifiHelper.isUsingWifi()) {
                    Log.d(TAG, "Not using WiFi, skipping auto-add")
                    return@launch
                }

                val currentSsid = wifiHelper.getWifiSsid()?.removeSurrounding("\"")
                if (currentSsid.isNullOrBlank()) {
                    Log.d(TAG, "No valid SSID found")
                    return@launch
                }

                val server = serverManager.getServer(serverId) ?: run {
                    Log.e(TAG, "Server not found for ID: $serverId")
                    return@launch
                }

                // Check if SSID is already in the list
                if (server.connection.internalSsids.contains(currentSsid)) {
                    Log.d(TAG, "SSID $currentSsid already in list")
                    return@launch
                }

                // Add new SSID to the list
                val updatedSsids = (server.connection.internalSsids + currentSsid).sorted()

                // Update server with new SSID list
                serverManager.updateServer(
                    server.copy(
                        connection = server.connection.copy(
                            internalSsids = updatedSsids
                        )
                    )
                )

                Log.d(TAG, "Successfully added SSID: $currentSsid")
            } catch (e: Exception) {
                Log.e(TAG, "Error auto-adding WiFi SSID", e)
            }
        }
    }

    /**
     * Automatically detect and add current WiFi SSID to all configured servers
     */
    fun autoAddCurrentWifiToAllServers() {
        val internalUrl = HassioUserSession.internalUrl ?: "";
        scope.launch {
            try {
                val servers = serverManager.defaultServers
                for (server in servers) {
                    autoAddCurrentWifi(server.id)
                    updateServerInternalUrl(server.id, internalUrl);
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error auto-adding WiFi SSID to all servers", e)
            }
        }
    }

    fun updateServerInternalUrl(serverId: Int, newInternalUrl: String) {
        scope.launch {
            try {
                val server = serverManager.getServer(serverId) ?: run {
                    Log.e(TAG, "Server not found for ID: $serverId")
                    return@launch
                }

                // Create updated server with new internal URL
                val updatedServer = server.copy(
                    connection = server.connection.copy(
                        internalUrl = newInternalUrl
                    )
                )

                // Update the server
                serverManager.updateServer(updatedServer)
                Log.d(TAG, "Successfully updated internal URL to: $newInternalUrl")

            } catch (e: Exception) {
                Log.e(TAG, "Error updating internal URL", e)
            }
        }
    }

    companion object {
        private const val TAG = "AutoWifiManager"
    }
}