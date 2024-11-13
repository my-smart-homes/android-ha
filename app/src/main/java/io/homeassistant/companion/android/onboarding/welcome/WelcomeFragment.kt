package io.homeassistant.companion.android.onboarding.welcome

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.onboarding.discovery.DiscoveryFragment
import io.homeassistant.companion.android.onboarding.login.LoginFragment
import io.homeassistant.companion.android.onboarding.manual.ManualSetupFragment
import io.homeassistant.companion.android.util.DialogUtils
import io.homeassistant.companion.android.util.VersionChecker
import io.homeassistant.companion.android.util.compose.HomeAssistantAppTheme
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Check for app update
        checkForAppUpdate()

        return ComposeView(requireContext()).apply {
            setContent {
                HomeAssistantAppTheme {
                    WelcomeView(
                        onContinue = { welcomeNavigation() }
                    )
                }
            }
        }
    }

    private fun checkForAppUpdate() {
        lifecycleScope.launch {
            VersionChecker.checkForUpdate(requireContext()) {
                DialogUtils.showUpdateDialog(requireContext())
            }
        }
    }

    private fun welcomeNavigation() {
        // goes to login
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.content, LoginFragment::class.java, null)
            .addToBackStack("Login")
            .commit()

        // went to discover fragment
        /*
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
        */
    }
}
