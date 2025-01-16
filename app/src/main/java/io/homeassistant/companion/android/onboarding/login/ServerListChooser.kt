package io.homeassistant.companion.android.onboarding.login
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.AlertDialog
import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import io.homeassistant.companion.android.R

class ServerListChooser {
    data class ServerItem(val name: String, val doc: DocumentSnapshot)

    companion object {
        lateinit var dialog: AlertDialog
        public suspend fun showServerSelectionDialog(
            context: Context,
            serverPasswords: List<DocumentSnapshot>
        ): DocumentSnapshot = suspendCoroutine { continuation ->
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)

            // Create custom layout for dialog
            val dialogView = inflater.inflate(R.layout.msh_server_selection_dialog, null)
            val listView = dialogView.findViewById<ListView>(R.id.server_list)

            // Custom adapter for the list
            class ServerAdapter(
                context: Context,
                private val servers: List<ServerItem>
            ) : ArrayAdapter<ServerItem>(context, R.layout.msh_server_list_item, servers) {

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: inflater.inflate(R.layout.msh_server_list_item, parent, false)

                    val serverIcon = view.findViewById<ImageView>(R.id.server_icon)
                    val serverName = view.findViewById<TextView>(R.id.server_name)
                    val serverUrl = view.findViewById<TextView>(R.id.server_url)
                    val serverId = view.findViewById<TextView>(R.id.server_id)

                    val server = servers[position]

                    serverIcon.setImageResource(R.drawable.app_icon_round)
                    serverName.text = server.name
                    serverId.text = server.doc.id

                    return view
                }
            }

            // Get server details asynchronously
            CoroutineScope(Dispatchers.Main).launch {
                val serverItems = mutableListOf<ServerItem>()

                serverPasswords.forEach { doc ->
                    val serverId = doc.id
                    try {
                        val serverDoc = FirebaseFirestore.getInstance()
                            .collection("servers")
                            .document(serverId)
                            .get()
                            .await()

                        val serverName = serverDoc.getString("homeName") ?: serverId
                        serverItems.add(ServerItem(serverName, doc))
                    } catch (e: Exception) {
                        serverItems.add(ServerItem(serverId, doc))
                    }
                }

                val adapter = ServerAdapter(context, serverItems)
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    dialog.dismiss()
                    continuation.resume(serverItems[position].doc)
                }

                dialog = builder.setTitle("Select Server")
                    .setView(dialogView)
                    .setOnCancelListener {
                        continuation.resumeWithException(Exception("Server selection cancelled"))
                    }
                    .create()
                dialog.show()
            }
        }
    }
}
