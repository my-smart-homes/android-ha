package io.homeassistant.companion.android.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import io.homeassistant.companion.android.util.compose.HomeAssistantAppTheme
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth


class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeAssistantAppTheme {
                    LoginView(
                        onLoginClick = { username, password -> loginUserWithFirebase(username, password)
                        }
                    )
                }
            }
        }
    }

    private fun loginUserWithFirebase(username: String, password: String) {
        val auth = FirebaseAuth.getInstance()

        if (validateCredentials(username, password)) {
            auth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
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
}

