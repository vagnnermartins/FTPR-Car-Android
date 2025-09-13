package com.example.myapitest.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapitest.ui.theme.Grey200_60
import com.example.myapitest.ui.theme.Grey500
import com.example.myapitest.ui.theme.Grey50_16
import com.example.myapitest.ui.theme.Grey800
import com.example.myapitest.ui.theme.Red400
import com.example.myapitest.ui.theme.MyApiTestTheme

@Composable
fun FormInput(
    modifier: Modifier = Modifier,
    value: String = "",
    label: String,
    onValueChanged: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    errorMessage: String = "",
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardImeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    placeholder: @Composable (() -> Unit)? = null,
    helperText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val hasError = errorMessage.isNotBlank()
    var isFocused by remember { mutableStateOf(false) }
    val borderWidth = if (isFocused && !hasError) 2.dp else 1.dp

    Column(modifier = modifier) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = Grey800)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = borderWidth,
                    color = when {
                        hasError -> Red400
                        isFocused -> Grey500
                        else -> Grey200_60
                    },
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                value = value,
                onValueChange = onValueChanged,
                maxLines = 1,
                enabled = enabled,
                readOnly = readOnly,
                isError = hasError,
                shape = RoundedCornerShape(
                    if (isFocused) 8.dp else 4.dp
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = keyboardCapitalization,
                    imeAction = keyboardImeAction,
                    keyboardType = keyboardType
                ),
                visualTransformation = visualTransformation,
                trailingIcon = trailingIcon,
                placeholder = placeholder,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Grey800,
                    unfocusedTextColor = Grey800,
                    disabledTextColor = Grey800,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Grey800,
                    errorContainerColor = Grey50_16,
                    errorTextColor = Grey800,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Grey50_16,
                    unfocusedContainerColor = Grey50_16,
                    cursorColor = Grey800,
                    focusedPlaceholderColor = Grey500,
                    unfocusedPlaceholderColor = Grey500,
                    disabledPlaceholderColor = Grey500
                )
            )
        }

        helperText?.let {
            Text(
                text = it,
                color = Grey500,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 8.dp),
                fontWeight = FontWeight.Normal
            )
        }

        if (hasError) {
            Text(
                text = errorMessage,
                color = Red400,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FormInputPreview() {
    MyApiTestTheme {
        var name by remember { mutableStateOf("Enter") }
        val errorMessage = if (name.isBlank()) "Informe o nome" else ""
        FormInput(
            value = name,
            label = "Email Address",
            onValueChanged = { newValue -> name = newValue },
            errorMessage = errorMessage,
            trailingIcon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
            },
            helperText = "Minimum 8 characters"
        )
    }
}
