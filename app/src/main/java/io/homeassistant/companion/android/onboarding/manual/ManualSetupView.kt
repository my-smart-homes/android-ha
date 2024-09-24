package io.homeassistant.companion.android.onboarding.manual

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import io.homeassistant.companion.android.common.R as commonR
import io.homeassistant.companion.android.onboarding.OnboardingHeaderView
import io.homeassistant.companion.android.onboarding.login.HassioUserSession
import kotlinx.coroutines.launch

@Composable
fun ManualSetupView(
    manualUrl: MutableState<String>,
    onManualUrlUpdated: (String) -> Unit,
    manualContinueEnabled: Boolean,
    connectedClicked: () -> Unit
) {
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val isLoading = rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val externalUrl = HassioUserSession.externalUrl ?: ""
        if (externalUrl.isNotEmpty() && isLoading.value == true) {
            manualUrl.value = externalUrl
            connectedClicked()  // Automatically trigger the connect button
            launch {
                kotlinx.coroutines.delay(1000)  // Delay for 1 second
                isLoading.value = false  // Hide overlay after delay
            }

        }
    }


    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OnboardingHeaderView(
            icon = CommunityMaterial.Icon3.cmd_web,
            title = stringResource(id = commonR.string.manual_title)
        )

        Text(
            text = stringResource(id = commonR.string.manual_desc),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        TextField(
            value = manualUrl.value,
            onValueChange = { onManualUrlUpdated(it) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            label = { Text(stringResource(id = commonR.string.input_url)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrect = false, keyboardType = KeyboardType.Uri),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    connectedClicked()
                }
            )
        )

        Button(
            enabled = manualContinueEnabled,
            onClick = connectedClicked,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text(stringResource(commonR.string.connect))
        }
    }

    if (isLoading.value) {
        Surface(
            color = MaterialTheme.colors.background.copy(alpha = 1f),  // Respect theme's background color
            modifier = Modifier.fillMaxSize()
        ) {

        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ManualSetupViewPreview() {
    ManualSetupView(
        manualUrl = remember {
            mutableStateOf("test")
        },
        onManualUrlUpdated = {},
        manualContinueEnabled = true,
        connectedClicked = {}
    )
}
