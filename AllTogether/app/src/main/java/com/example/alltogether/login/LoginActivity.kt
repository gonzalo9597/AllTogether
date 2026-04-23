package com.example.alltogether.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.alltogether.R
import com.example.alltogether.couples.MisParejasActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.network.ApiClient
import com.example.alltogether.network.LoginResponse
import com.example.alltogether.register.RegisterActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.GoogleAuthHelper
import com.example.alltogether.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllTogetherTheme {
                PantallaLogin()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin() {
    val context = LocalContext.current
    val service = remember { AllTogetherService() }
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()
    val activity = context as ComponentActivity
    val sesionCaducada = activity.intent.getBooleanExtra("sesion_caducada", false)
    val lifecycleOwner = LocalLifecycleOwner.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var cargandoGoogle by remember { mutableStateOf(false) }
    val googleAuthHelper = remember { GoogleAuthHelper(context as ComponentActivity) }

    fun navegarAParejas(loginResponse: LoginResponse) {
        sessionManager.saveSession(
            token = loginResponse.token,
            userId = loginResponse.idUsuario,
            userName = loginResponse.nombre,
            email = loginResponse.email
        )
        ApiClient.token = loginResponse.token
        ApiClient.appContext = context.applicationContext
        context.startActivity(Intent(context, MisParejasActivity::class.java))
        (context as ComponentActivity).finish()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                email = ""
                password = ""
                error = ""
                cargando = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondologo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo AllTogether",
                            modifier = Modifier.size(width = 210.dp, height = 74.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = VerdePrincipal
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VerdePrincipal
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "AllTogether",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (sesionCaducada) {
                            Text(
                                text = "Tu sesión ha caducado. Por favor inicia sesión de nuevo.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña", color = Color.Black.copy(alpha = 0.85f)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        HorizontalDivider(
                            thickness = 1.dp,
                            color = VerdeSuave.copy(alpha = 0.55f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    error = "Introduce email y contraseña"
                                    return@Button
                                }
                                cargando = true
                                error = ""
                                coroutineScope.launch {
                                    val resultado = withContext(Dispatchers.IO) {
                                        service.login(email, password)
                                    }
                                    resultado
                                        .onSuccess { navegarAParejas(it) }
                                        .onFailure {
                                            error = "Email o contraseña incorrectos"
                                            cargando = false
                                        }
                                }
                            },
                            enabled = !cargando && !cargandoGoogle,  // 👈 cambio 1
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (cargando) {
                                CircularProgressIndicator(
                                    color = Color.Black,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Iniciar sesión",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 👈 cambio 2 — botón Google
                        Button(
                            onClick = {
                                cargandoGoogle = true
                                error = ""
                                coroutineScope.launch {
                                    val idToken = googleAuthHelper.signIn()
                                    if (idToken != null) {
                                        val resultado = withContext(Dispatchers.IO) {
                                            service.loginGoogle(idToken)
                                        }
                                        resultado
                                            .onSuccess { navegarAParejas(it) }
                                            .onFailure {
                                                error = "Error al iniciar sesión con Google"
                                                cargandoGoogle = false
                                            }
                                    } else {
                                        error = "No se pudo obtener el token de Google"
                                        cargandoGoogle = false
                                    }
                                }
                            },
                            enabled = !cargando && !cargandoGoogle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (cargandoGoogle) {
                                CircularProgressIndicator(color = Color.Black, strokeWidth = 2.5.dp)
                            } else {
                                Text("Continuar con Google", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "o",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                context.startActivity(Intent(context, RegisterActivity::class.java))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Crear cuenta")
                        }

                        if (error.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = error,
                                color = Color.Black,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}