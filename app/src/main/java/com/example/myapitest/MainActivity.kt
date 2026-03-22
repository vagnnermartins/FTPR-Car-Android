package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapitest.ui.theme.CarLocationAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.squareup.picasso.Picasso

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarLocationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val user = viewModel.currentUser
    val isLoading = viewModel.isLoading

    // Configuração do Google Sign-In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                viewModel.loginWithGoogle(token) { success ->
                    if (success) {
                        Toast.makeText(context, "Logado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: ApiException) {
            Log.e("GoogleSignIn", "Erro: ${e.statusCode} - ${e.message}")
            Toast.makeText(context, "Erro Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    if (user == null) {
        LoginContent(
            modifier = modifier,
            isLoading = isLoading,
            onSignInClick = { launcher.launch(googleSignInClient.signInIntent) }
        )
    } else {
        UserProfileContent(
            userName = user.displayName ?: "Usuário",
            userEmail = user.email ?: "",
            photoUrl = user.photoUrl?.toString(),
            onViewCarsClick = {
                context.startActivity(Intent(context, ListActivity::class.java))
            },
            onLogout = { viewModel.logout(googleSignInClient) }
        )
    }
}

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Car Location App",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = "Faça login para continuar", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedButton(
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Entrar com Google")
            }
        }
    }
}

@Composable
fun UserProfileContent(
    userName: String,
    userEmail: String,
    photoUrl: String?,
    onViewCarsClick: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            update = { imageView ->
                Picasso.get()
                    .load(photoUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageView)
            },
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = userName, style = MaterialTheme.typography.headlineMedium)
        Text(text = userEmail, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onViewCarsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Ver Lista de Carros")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onLogout,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Sair da Conta")
        }
    }
}
