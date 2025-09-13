package com.example.myapitest.ui.login.loginForm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapitest.MainActivity
import com.example.myapitest.ui.composables.AppBar
import com.example.myapitest.ui.composables.FormInput
import com.example.myapitest.ui.theme.LightGray
import com.example.myapitest.ui.theme.MyApiTestTheme
import com.example.myapitest.ui.theme.White
import com.example.myapitest.ui.theme.White20
import com.example.myapitest.ui.theme.White30
import com.example.myapitest.network.StatusService
import com.example.myapitest.ui.theme.Grey800

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onContinuePressed: () -> Unit = {},
    viewModel: LoginViewModel
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(White, LightGray)
                )
            ),
        containerColor = White30,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppBar(
                title = "Log in",
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1.0F)
            ) {
                Column(
                    modifier = Modifier
                        .background(color = White20)
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    FormInput(
                        value = viewModel.phoneNumber,
                        label = "Phone Number",
                        onValueChanged = { viewModel.updatePhoneNumber(it) },
//                        errorMessage = viewModel.,
                        placeholder = {
                            Text(text = "Enter")
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = White30)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (viewModel.loginState) {
                    is StatusService.Loading -> CircularProgressIndicator()
                    is StatusService.Success<*> -> {
                        viewModel.updateState(StatusService.Idle)
                        onContinuePressed.invoke()
                        ContinueButton(viewModel)
                    }
                    is StatusService.Error -> ContinueButton(viewModel)
                    is StatusService.Idle -> ContinueButton(viewModel)
                    is StatusService.UnknownError -> Text("Error")
                }
            }
        }
    }
}

@Composable
fun ContinueButton(viewModel: LoginViewModel) {
    val ctx = LocalContext.current
    val activity = ctx as MainActivity
    Button(
        onClick = {
            viewModel.sendVerificationCode(activity)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(1f),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Grey800,
            contentColor = Color.White,
            disabledContainerColor = Grey800,
            disabledContentColor = Color.White
        )
    ) {
        Text(
            text = "Continue",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview(modifier: Modifier = Modifier) {
    MyApiTestTheme {
        val viewModel: LoginViewModel = viewModel()
        LoginScreen(viewModel = viewModel)
    }
}