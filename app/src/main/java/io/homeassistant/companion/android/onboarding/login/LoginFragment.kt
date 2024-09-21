package io.homeassistant.companion.android.onboarding.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import io.homeassistant.companion.android.util.compose.HomeAssistantAppTheme

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeAssistantAppTheme {
                    LoginView(
                        onLoginClick = { username, password ->
                            // Print login information via Toast
                            Toast.makeText(
                                requireContext(),
                                "Username: $username, Password: $password",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }
        }
    }
}