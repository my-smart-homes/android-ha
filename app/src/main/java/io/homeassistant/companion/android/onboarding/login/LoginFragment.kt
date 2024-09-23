package io.homeassistant.companion.android.onboarding.login

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
            isLoading = true
            val auth = FirebaseAuth.getInstance()

            // Use coroutines to handle Firebase calls
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(username, password).await()
                    val userId = auth.currentUser?.uid
                    isLoading = false

                    if (userId != null) {
                        // Fetch the external URL, webview username, and password from Firebase
                        val webviewCredentials = getWebViewCredentials(userId)
                        if (webviewCredentials != null) {
                            Log.d("Firestore", "External URL: ${webviewCredentials.externalUrl}")
                            Log.d("Firestore", "Webview Username: ${webviewCredentials.username}")
                            Log.d("Firestore", "Webview Password: ${webviewCredentials.password}")

                            // Save credentials to UserSession
                            UserSession.externalUrl = webviewCredentials.externalUrl
                            UserSession.webviewUsername = webviewCredentials.username
                            UserSession.webviewPassword = webviewCredentials.password

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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

    data class WebviewCredentials(val externalUrl: String?, val username: String?, val password: String?)

    private suspend fun getWebViewCredentials(userId: String): WebviewCredentials? {
        val db = FirebaseFirestore.getInstance()
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                val externalUrl = document.getString("external_url")
                val webviewUsername = document.getString("webview_username")
                val webviewPassword = document.getString("webview_password")
                WebviewCredentials(externalUrl, webviewUsername, webviewPassword)
            } else {
                Log.d("Firestore", "No such document.")
                null
            }
        } catch (e: Exception) {
            Log.d("Firestore", "Error getting document: ", e)
            null
        }
    }

}

