package com.example.alltogether.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.couples.MisParejasActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.network.ApiClient
import com.example.alltogether.register.RegisterActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.GoogleAuthHelper
import com.example.alltogether.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FondoPantalla = Color.Black
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)
private val GrisTexto = Color(0xFF1F1F1F)

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
    val googleAuthHelper = remember { GoogleAuthHelper(context as ComponentActivity) }
    val coroutineScope = rememberCoroutineScope()
    val activity = context as ComponentActivity
    val sesionCaducada = activity.intent.getBooleanExtra("sesion_caducada", false)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var cargandoGoogle by remember { mutableStateOf(false) }

    fun navegarAParejas(loginResponse: com.example.alltogether.network.LoginResponse) {
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

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AllTogether",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VerdePrincipal)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoPantalla)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (sesionCaducada) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = VerdeSuave)
                ) {
                    Text(
                        text = "Tu sesión ha caducado. Por favor inicia sesión de nuevo.",
                        color = GrisTexto,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VerdePrincipal),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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
                        enabled = !cargando && !cargandoGoogle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = Color.Black, strokeWidth = 2.5.dp)
                        } else {
                            Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = VerdeSuave.copy(alpha = 0.55f))
                        Text(
                            text = "  o  ",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = VerdeSuave.copy(alpha = 0.55f))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Google
                    Button(
                        onClick = {
                            cargandoGoogle = true
                            error = ""
                            coroutineScope.launch {
                                android.util.Log.d("GoogleAuth", "Iniciando flujo Google")

                                val idToken = googleAuthHelper.signIn()
                                android.util.Log.d("GoogleAuth", "idToken recibido: $idToken")

                                if (idToken != null) {
                                    val resultado = withContext(Dispatchers.IO) {
                                        service.loginGoogle(idToken)
                                    }
                                    resultado
                                        .onSuccess {
                                            android.util.Log.d("GoogleAuth", "Login exitoso: ${it.nombre}")
                                            navegarAParejas(it) }
                                        .onFailure {
                                            android.util.Log.e("GoogleAuth", "Error login Lambda: ${it.message}", it)

                                            error = "Error al iniciar sesión con Google"
                                            cargandoGoogle = false
                                        }
                                } else {
                                    android.util.Log.e("GoogleAuth", "idToken es null")

                                    error = "No se pudo obtener el token de Google"
                                    cargandoGoogle = false
                                }
                            }
                        },
                        enabled = !cargando && !cargandoGoogle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
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

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(thickness = 1.dp, color = VerdeSuave.copy(alpha = 0.55f))

                    Spacer(modifier = Modifier.height(18.dp))

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
                        Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
                    }

                    if (error.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}