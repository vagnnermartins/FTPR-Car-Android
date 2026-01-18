package com.utfpr.psil.cartrack.ui.screens

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utfpr.psil.cartrack.R
import com.utfpr.psil.cartrack.ui.model.AuthUiEvents
import com.utfpr.psil.cartrack.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onSuccessfullyLoginOnGoogle: () -> Unit = {},
    onSmsLogin: () -> Unit = {}
) {

    val context = LocalContext.current
    val authUiEvents by authViewModel.uiAuthEvents.collectAsState()
    val isLoading = authUiEvents is AuthUiEvents.Loading

    LaunchedEffect(authUiEvents) {
        when (val event = authUiEvents) {
            is AuthUiEvents.Success -> onSuccessfullyLoginOnGoogle()
            is AuthUiEvents.Error -> {
                Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                authViewModel.resetEvents()
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null
            )
            Text(
                modifier = Modifier
                    .padding(vertical = 16.dp),
                text = stringResource(R.string.app_name),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                text = stringResource(R.string.login_app_description)
            )
            LoginButton(
                buttonDescription = R.string.btn_login_google,
                icon = R.drawable.ic_google_login,
                enabled = !isLoading,
                onClick = {
                    authViewModel.loginWithGoogle(context)
                },
                buttonColors = ButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                )
            )
            LoginButton(
                buttonDescription = R.string.btn_login_sms,
                icon = R.drawable.ic_sms_login,
                enabled = !isLoading,
                onClick = onSmsLogin,
                buttonColors = ButtonColors(
                    containerColor = Color(0xff1e1f57),
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                )
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(50.dp),
                color = Color(0xff1e1f57)
            )
        }
    }
}

@Composable
private fun LoginButton(
    @StringRes buttonDescription: Int,
    @DrawableRes icon: Int,
    buttonColors: ButtonColors,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = buttonColors,
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(stringResource(buttonDescription))
        }
    }
}
