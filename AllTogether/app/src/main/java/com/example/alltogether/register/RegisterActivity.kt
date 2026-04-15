package com.example.alltogether.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.couples.MisParejasActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.network.ApiClient
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ScreenUiState
import com.example.alltogether.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FondoPantalla = Color.Black
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)
private val GrisTexto = Color(0xFF1F1F1F)

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllTogetherTheme {
                PantallaRegister()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegister() {
    val context = LocalContext.current
    val service = remember { AllTogetherService() }
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // Desactiva el botón mientras se procesa la petición
    var cargando by remember { mutableStateOf(false) }

    var mostrarCardInfo by remember { mutableStateOf(ScreenUiState.mostrarCardInfoRegister) }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            mostrarCardInfo = false
            ScreenUiState.mostrarCardInfoRegister = false
        }
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Crear cuenta",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Crear cuenta",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color.White
                        )
                    }
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
                .background(FondoPantalla)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AnimatedVisibility(visible = mostrarCardInfo) {
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {},
                    content = {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = VerdeSuave
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Text(
                                    text = "Empieza en AllTogether",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GrisTexto
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Crea tu cuenta para empezar a gestionar gastos y parejas dentro de la app",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GrisTexto
                                )
                            }
                        }
                    }
                )
            }

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
                        text = "Tus datos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre", color = Color.Black.copy(alpha = 0.85f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                        // Oculta la contraseña con puntos
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
                            if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
                                error = "Rellena todos los campos"
                                return@Button
                            }

                            cargando = true
                            error = ""

                            coroutineScope.launch {
                                // La llamada de red siempre en IO, nunca en el hilo principal
                                val resultado = withContext(Dispatchers.IO) {
                                    service.register(nombre, email, password)
                                }

                                resultado
                                    .onSuccess { loginResponse ->
                                        // Guardar token cifrado en disco
                                        sessionManager.saveSession(
                                            token = loginResponse.token,
                                            userId = loginResponse.idUsuario,
                                            userName = loginResponse.nombre,
                                            email = loginResponse.email
                                        )
                                        // Inyectar token en el cliente HTTP para esta sesión
                                        ApiClient.token = loginResponse.token
                                        ApiClient.appContext = context.applicationContext

                                        // Tras el registro vamos directamente a MisParejas
                                        // sin pasar por Login — el token ya está listo
                                        context.startActivity(
                                            Intent(context, MisParejasActivity::class.java)
                                        )
                                        (context as ComponentActivity).finish()
                                    }
                                    .onFailure {
                                        error = "Error al crear la cuenta. El email puede que ya esté registrado."
                                        cargando = false
                                    }
                            }
                        },
                        enabled = !cargando,
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
                                text = "Crear cuenta",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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