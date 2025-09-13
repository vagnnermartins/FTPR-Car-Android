package com.example.myapitest.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapitest.R
import com.example.myapitest.ui.theme.Black
import com.example.myapitest.ui.theme.Grey800
import com.example.myapitest.ui.theme.MyApiTestTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color = Grey800,
    onBackPressed: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                ambientColor = Color(0xC3CBD266)
            ),
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                color = titleColor
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Grey800,
            navigationIconContentColor = Grey800,
            actionIconContentColor = Grey800
        ),
        navigationIcon = {
            if (onBackPressed != null) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        actions =  { action?.invoke() }
    )
}

@Preview(showBackground = true)
@Composable
private fun AppBarPreview() {
    MyApiTestTheme {
        AppBar(
            title = "Log in",
            onBackPressed = {},
            action = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    tint = Black,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )
            }
        )
    }
}