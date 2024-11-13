package io.homeassistant.companion.android.util

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import android.net.Uri

object DialogUtils {

    fun showUpdateDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("Update Available")
            setMessage("A new version is available. Please update to continue.")
            setPositiveButton("Update") { _, _ ->
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://my-smart-homes.github.io/app-landing/"))
                context.startActivity(browserIntent)
            }
            setNegativeButton("Later", null)
            show()
        }
    }
}
