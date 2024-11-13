package io.homeassistant.companion.android.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class VersionChecker {

    companion object {
        val THIS_APP_VERSION_CODE = 1;
        private val VERSION_URL = "https://my-smart-homes.github.io/app-landing/version.json?rand=${System.currentTimeMillis()}"

        suspend fun checkForUpdate(context: Context, onUpdateAvailable: () -> Unit) {
            try {
                val url = URL(VERSION_URL)
                withContext(Dispatchers.IO) {
                    (url.openConnection() as? HttpURLConnection)?.run {
                        requestMethod = "GET"
                        inputStream.bufferedReader().use {
                            val response = it.readText()
                            val jsonObject = JSONObject(response)
                            val latestVersionCode = jsonObject.getInt("latest_version_code")
                            if (latestVersionCode > THIS_APP_VERSION_CODE) {
                                withContext(Dispatchers.Main) { onUpdateAvailable() }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VersionChecker", "Failed to check version", e)
            }
        }
    }
}
