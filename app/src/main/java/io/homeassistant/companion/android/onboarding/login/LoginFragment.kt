package io.homeassistant.companion.android.onboarding.login

import ServerTimeFetchService
import ServerTimeFetchServiceImpl
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import io.homeassistant.companion.android.util.compose.HomeAssistantAppTheme
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.onboarding.discovery.DiscoveryFragment
import io.homeassistant.companion.android.onboarding.manual.ManualSetupFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginFragment : Fragment() {

    private var isLoading by mutableStateOf(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return ComposeView(requireContext()).apply {
            setContent {
                HomeAssistantAppTheme {
                    LoginView(
                        onLoginClick = { username, password -> loginUserWithFirebase(username, password)},
                        isLoading = isLoading
                    )
                }
            }
        }
    }

    private fun loginUserWithFirebase(username: String, password: String) {
        if (validateCredentials(username, password)) {
            Log.d("Firestore", "loginUserWithFirebase")
            isLoading = true
            val auth = FirebaseAuth.getInstance()
            val serverTimeService: ServerTimeFetchService = ServerTimeFetchServiceImpl()
            // Use coroutines to handle Firebase calls
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(username, password).await()
                    Log.d("Firestore", "SignInSuccess2")

                    val userId = auth.currentUser?.uid
                    Log.d("Firestore", "user id ${userId}")
                    isLoading = false

                    if (userId != null) {
                        Log.d("Firestore", "calling getWebViewCredentials")
                        // Fetch the external URL, webview username, and password from Firebase
                        val webviewCredentials = getWebViewCredentials(userId)
                        Log.d("Firestore", "result getWebViewCredentials ${webviewCredentials?.expirationDate}")

                        if (webviewCredentials != null) {
                            val currentTime = serverTimeService.fetchServerTime()

                            if (currentTime == null) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(requireContext(), "Failed to fetch server time", Toast.LENGTH_LONG).show()
                                }
                                return@launch
                            }

                            // Check expiration date
                            if (webviewCredentials.expirationDate == null || webviewCredentials.expirationDate < currentTime) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(requireContext(), "You don't have a subscription", Toast.LENGTH_LONG).show()
                                }
                                FirebaseAuth.getInstance().signOut()
                                return@launch
                            }

                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                            }

                            Log.d("Firestore", "External URL: ${webviewCredentials.externalUrl}")
                            Log.d("Firestore", "Webview Username: ${webviewCredentials.username}")
                            Log.d("Firestore", "Webview Password: ${webviewCredentials.password}")

                            // Save credentials to UserSession
                            HassioUserSession.externalUrl = webviewCredentials.externalUrl
                            HassioUserSession.webviewUsername = webviewCredentials.username
                            // HassioUserSession.webviewPassword = webviewCredentials.password

                            if(webviewCredentials.password.isNullOrEmpty()){
                                Log.e("LoginFragment", "webviewCredentials Password is null or empty")
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(requireContext(), "WebCred Pass is Empty", Toast.LENGTH_SHORT).show()
                                }
                                return@launch
                            }

                            try {
                                // Create an instance of CryptoUtil
                                val cryptoUtil = CryptoUtil()

                                // Attempt to decrypt the password
                                val decryptedPassword = cryptoUtil.aes256CbcPkcs7Decrypt(
                                webviewCredentials.password.toString()
                                )

                                // Save decrypted password to the session
                                HassioUserSession.webviewPassword = decryptedPassword

                            } catch (e: Exception) {
                                // Log error and show a Toast message for decryption failure
                                Log.e("CryptoUtil", "Password decryption failed: ${e.message}")
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(requireContext(), "Password decryption failed", Toast.LENGTH_LONG).show()
                                }
                                return@launch // Exit if decryption fails
                            }

                            loginNavigation() // Proceed with the next step
                        } else {
                            Log.d("Firestore", "No webview credentials found for this user.")
                        }
                    } else {
                        Log.d("FirebaseAuth", "User ID is null after login.")
                    }
                } catch (e: Exception) {
                    isLoading = false
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loginNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && HassioUserSession.externalUrl == null) {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.content, DiscoveryFragment::class.java, null)
                .addToBackStack("Welcome")
                .commit()
        } else {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.content, ManualSetupFragment::class.java, null)
                .addToBackStack("Welcome")
                .commit()
        }
    }

    private fun validateCredentials(username: String, password: String): Boolean {
        return when {
            username.isEmpty() -> {
                Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_LONG).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_LONG).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(username).matches() -> {
                Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    data class WebviewCredentials(
        val externalUrl: String?,
        val username: String?,
        val password: String?,
        val expirationDate: Long?
    )

    private suspend fun getWebViewCredentials(userId: String): WebviewCredentials? {
        val db = FirebaseFirestore.getInstance()
        Log.d("Firestore", "getWebViewCredentials init db")
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                // Get the email as the username
                val webviewUsername = userDoc.getString("email")
                // Get the expiration date from subscription.expiresAt
                val expirationDate = userDoc.getTimestamp("subscription.expiresAt")?.toDate()?.time

                var webviewPassword: String? = null
                var externalUrl: String? = null

                // Access the serverPasswords subcollection and get the first document
                val serverPasswordsCollection = userDoc.reference.collection("serverPasswords")
                val serverPasswordsSnapshot = serverPasswordsCollection.get().await()

                Log.d("Firestore", "serverpass size: ${serverPasswordsSnapshot.size()}")
                var firstServerPasswordDoc: DocumentSnapshot?
                if (serverPasswordsSnapshot.documents.size > 1) {
                    Log.d("Firestore", "Multiple serverPasswords found for user: $userId")
                    val selectedDoc = ServerListChooser.showServerSelectionDialog(requireContext(), serverPasswordsSnapshot.documents)
                    firstServerPasswordDoc = selectedDoc
                } else {
                        firstServerPasswordDoc = serverPasswordsSnapshot.documents.firstOrNull()
                }

//                throw Exception("MANUAL BREAK")

                if (firstServerPasswordDoc != null) {
                    // Get the encrypted password
                    webviewPassword = firstServerPasswordDoc.getString("encryptedPass")

                    // Get the server ID (document ID)
                    val serverId = firstServerPasswordDoc.id
                    Log.d("Firestore", "First Server DOC: $serverId")

                    // Access the servers collection to get the externalUrl
                    val serverDoc = db.collection("servers").document(serverId).get().await()
                    if (serverDoc.exists()) {
                        externalUrl = serverDoc.getString("externalUrl")

                        // After retrieving externalUrl from serverDoc
                        if (externalUrl != null && !externalUrl.startsWith("http://") && !externalUrl.startsWith("https://")) {
                            externalUrl = "https://$externalUrl"
                        }

                    } else {
                        Log.d("Firestore", "No server found with ID: $serverId")
                    }
                } else {
                    Log.d("Firestore", "No serverPasswords documents for user: $userId")
                }

                Log.d(
                    "Firestore:LoginFragment",
                    "webviewPassword: $webviewPassword, expirationDate: $expirationDate, externalUrl: $externalUrl"
                )

                WebviewCredentials(externalUrl, webviewUsername, webviewPassword, expirationDate)
            } else {
                Log.d("Firestore", "No such document.")
                null
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting document: ", e)
            null
        }
    }
}

