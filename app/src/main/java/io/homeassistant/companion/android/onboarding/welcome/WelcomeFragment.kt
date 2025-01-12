package io.homeassistant.companion.android.onboarding.welcome

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.homeassistant.companion.android.R
import io.homeassistant.companion.android.common.util.LocationPermissionInfoHandler
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
        // check location permission
        val permissionsToCheck = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11 and above
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
            else -> {
                // Below Android 10
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }

        if (!checkPermissions(permissionsToCheck)) {
            // Show info dialog and request permissions if needed
            LocationPermissionInfoHandler.showLocationPermInfoDialogIfNeeded(
                requireContext(),
                permissionsToCheck,
                continueYesCallback = {
                    requestLocationPermission()
                    // Optionally handle what happens after requesting permissions
                },
                continueNoCallback = {
                    navigateToLogin()
                }
            )
        }else{
            navigateToLogin()
        }

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

    private fun navigateToLogin() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.content, LoginFragment::class.java, null)
            .addToBackStack("Login")
            .commit()
    }


    // Helper function to check if permissions are granted
    private fun checkPermissions(permissions: Array<String>): Boolean {
        var allPermissionsGranted = true
        permissions.forEach { permission ->
            val granted = ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
            Log.d("WelcomeFragment", "Permission $permission granted: $granted")
            if (!granted) {
                allPermissionsGranted = false
            }
        }
        return allPermissionsGranted
    }

    // Existing function to request permissions (no change needed)
    private fun requestLocationPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION) // Background location will be requested later
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        permissionsRequest.launch(permissions)
    }

    private val permissionsRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        navigateToLogin()
    }
}
