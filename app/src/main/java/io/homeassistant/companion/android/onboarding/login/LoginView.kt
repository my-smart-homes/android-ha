package io.homeassistant.companion.android.onboarding.login

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.homeassistant.companion.android.R

@Preview
@Composable
fun LoginView(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    isLoading: Boolean = false
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.TopCenter) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.44f),
                    painter = painterResource(id = R.drawable.login_bg_shape),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                )

                Row(
                    modifier = Modifier.padding(top = 55.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Icon(
                        tint = Color.White,
                        modifier = Modifier.size(100.dp),
                        painter = painterResource(id = R.drawable.my_smart_home_icon), contentDescription = null)
                }

                Text(
                    style = MaterialTheme.typography.h5.copy(
                        color = colorResource(id = io.homeassistant.companion.android.common.R.color.colorPrimary),
                    ),
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(alignment = Alignment.BottomCenter),
                    text = stringResource(id = io.homeassistant.companion.android.common.R.string.login),
                )
            }

            Spacer(modifier = Modifier.height(5.dp))  // Add some space below the title

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,  // Set keyboard type to Email
                        imeAction = ImeAction.Next
                    ),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,  // Set keyboard type to Password
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation()
                )
                if(isLoading){
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                }else{
                    Button(
                        onClick = { onLoginClick(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = colorResource(id = io.homeassistant.companion.android.common.R.color.colorPrimary) // Set your custom background color here
                        )
                    ) {
                        Text(
                            stringResource(id = io.homeassistant.companion.android.common.R.string.login),
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 10.dp) // Adjust the value as needed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            val context = LocalContext.current

            // Add "Need Help?" text at the bottom
            Text(
                text = "Need Help?",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
                    .clickable {
                        // Launch the URL in an external browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mysmarthomes.us/"))
                        context.startActivity(intent)
                    },
                color = colorResource(id = io.homeassistant.companion.android.common.R.color.colorPrimary),

                )
        }
    }
}
