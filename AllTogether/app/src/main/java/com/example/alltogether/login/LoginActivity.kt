package com.example.alltogether.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.example.alltogether.register.RegisterActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FondoPantalla = Color.Black
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

    // Controla si el botón está desactivado mientras se hace la petición
    // para evitar que el usuario pulse dos veces
    var cargando by remember { mutableStateOf(false) }

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

    Scaffold(
        containerColor = FondoPantalla,
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
        },
        bottomBar = {
            PieInicio()
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
                        // Oculta la contraseña con puntos en lugar de texto visible
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
                            // Evitar peticiones vacías
                            if (email.isBlank() || password.isBlank()) {
                                error = "Introduce email y contraseña"
                                return@Button
                            }

                            cargando = true
                            error = ""

                            coroutineScope.launch {
                                // La llamada de red siempre en IO, nunca en el hilo principal
                                val resultado = withContext(Dispatchers.IO) {
                                    service.login(email, password)
                                }

                                resultado
                                    .onSuccess { loginResponse ->
                                        // 1. Guardar token cifrado en disco para futuras sesiones
                                        sessionManager.saveSession(
                                            token = loginResponse.token,
                                            userId = loginResponse.idUsuario,
                                            userName = loginResponse.nombre,
                                            email = loginResponse.email
                                        )
                                        // 2. Inyectar token en el cliente HTTP para esta sesión
                                        ApiClient.token = loginResponse.token
                                        ApiClient.appContext = context.applicationContext
                                        // 3. Navegar a la pantalla principal
                                        context.startActivity(
                                            Intent(context, MisParejasActivity::class.java)
                                        )
                                        (context as ComponentActivity).finish()
                                    }
                                    .onFailure {
                                        error = "Email o contraseña incorrectos"
                                        cargando = false
                                    }
                            }
                        },
                        // Desactivar el botón mientras carga para evitar doble envío
                        enabled = !cargando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Mostrar spinner mientras carga, texto normal cuando no
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

@Composable
fun PieInicio() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Proyecto Intermodular de DAM · AllTogether",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
        Text(
            text = "Sergio Malón, Sergio Sanz, Gonzalo Sebastián · v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
        Text(
            text = "2025-2026",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}