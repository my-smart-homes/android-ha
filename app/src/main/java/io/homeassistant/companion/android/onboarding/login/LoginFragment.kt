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

        val auth = FirebaseAuth.getInstance()

        if (validateCredentials(username, password)) {
            isLoading = true
            auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_LONG).show()
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            getExternalUrl(userId) { externalUrl ->
                                if (externalUrl != null) {
                                    getExternalUrl(userId) { externalUrl ->
                                        if (externalUrl != null) {
                                            Log.d("Firestore", "External URL: $externalUrl")
                                            // Handle the external URL here (e.g., show a Toast or navigate)
                                            Toast.makeText(requireContext(), "External URL: $externalUrl", Toast.LENGTH_LONG).show()
                                            loginNavigation()
                                        } else {
                                            Log.d("Firestore", "No external URL found for this user.")
                                        }
                                    }
                                    Log.d("Firestore", "External URL: $externalUrl")
                                } else {
                                    Log.d("Firestore", "No external URL found for this user.")
                                }
                            }
                        } else {
                            Log.d("FirebaseAuth", "User ID is null after login.")
                        }
//                       loginNavigation()
                    } else {
                        Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
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

    fun getExternalUrl(userId: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)

        // Fetch the document
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get the 'external_url' field as a String
                    val externalUrl = document.getString("external_url")
                    // Return the value via the callback
                    callback(externalUrl)
                } else {
                    Log.d("Firestore", "No such document.")
                    callback(null)  // No document found
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting document: ", exception)
                callback(null)  // Error occurred
            }
    }

}

