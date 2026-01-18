package com.utfpr.psil.cartrack.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.utfpr.psil.cartrack.R
import com.utfpr.psil.cartrack.ui.model.AuthUiEvents
import com.utfpr.psil.cartrack.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsCodeVerificationScreen(
    authViewModel: AuthViewModel,
    onBackButtonPress: () -> Unit,
    onSuccessfulVerification: () -> Unit
) {
    val authUiState by authViewModel.uiAuthState.collectAsState()
    val authUiEvents by authViewModel.uiAuthEvents.collectAsState()

    var code by remember { mutableStateOf("") }
    var isCodeComplete by remember { mutableStateOf(false) }

    LaunchedEffect(authUiEvents) {
        when (authUiEvents) {
            is AuthUiEvents.Success -> onSuccessfulVerification()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text(stringResource(R.string.top_bar_sms_code_verification_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackButtonPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor = Color(0xff1e1f57),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                    scrolledContainerColor = Color(0xff1e1f57),
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SmsCodeInput(
                authUiState.phoneNumber.toString(),
                onCodeChange = { newCode, isCompleted ->
                    code = newCode
                    isCodeComplete = isCompleted
                }
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    authViewModel.verifyCode(authUiState.verificationId, code)
                },
                enabled = isCodeComplete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonColors(
                    containerColor = Color(0xff1e1f57),
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.DarkGray
                )
            ) {
                Text(stringResource(R.string.btn_verify_code_sms))
            }
        }

    }
}

@Composable
fun SmsCodeInput(
    phoneNumber: String,
    onCodeChange: (code: String, isCompleted: Boolean) -> Unit
) {

    var code by remember { mutableStateOf(List(CODE_LENGTH) { "" }) }
    val focusRequesters = remember { List(CODE_LENGTH) { FocusRequester() } }

    LaunchedEffect(code) {
        val newCode = code.joinToString("")
        onCodeChange(newCode, newCode.length == CODE_LENGTH)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.verification_screen_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(R.string.verification_sms_description, phoneNumber),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (codeDigitIndex in 0 until CODE_LENGTH) {
                DigitInput(
                    value = code[codeDigitIndex],
                    onValueChange = { newValue ->
                        if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
                            val newCodeList = code.toMutableList()
                            newCodeList[codeDigitIndex] = newValue
                            code = newCodeList

                            if (newValue.isNotEmpty() && codeDigitIndex < CODE_LENGTH - 1) {
                                focusRequesters[codeDigitIndex + 1].requestFocus()
                            }
                        }
                    },
                    focusRequester = focusRequesters[codeDigitIndex],
                    onBackspacePress = {
                        if (code[codeDigitIndex].isEmpty() && codeDigitIndex > 0) {
                            val newCodeList = code.toMutableList()
                            newCodeList[codeDigitIndex - 1] = ""
                            code = newCodeList
                            focusRequesters[codeDigitIndex - 1].requestFocus()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DigitInput(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onBackspacePress: () -> Unit
) {
    var digitCodeFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    BasicTextField(
        value = digitCodeFieldValue,
        onValueChange = {
            if (it.text.length <= 1) {
                digitCodeFieldValue = it
                onValueChange(it.text)
            }
        },
        modifier = Modifier
            .size(50.dp)
            .focusRequester(focusRequester)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .onKeyEvent { event ->
                if (event.key == Key.Backspace && event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    onBackspacePress()
                    true
                } else {
                    false
                }
            },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                innerTextField()
            }
        }
    )
}

const val CODE_LENGTH = 6