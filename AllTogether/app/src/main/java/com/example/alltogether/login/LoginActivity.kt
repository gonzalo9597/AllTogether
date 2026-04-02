package com.example.alltogether.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.alltogether.couples.MisParejasActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.network.ApiClient
import com.example.alltogether.register.RegisterActivity
import com.example.alltogether.util.SessionManager
import com.example.alltogether.ui.theme.AllTogetherTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

@Composable
fun PantallaLogin() {
    val context = LocalContext.current
    val service = remember { AllTogetherService() }
    val sessionManager = remember { SessionManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // Controla si el botón está desactivado mientras se hace la petición
    // para evitar que el usuario pulse dos veces
    var cargando by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "AllTogether",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                // Oculta la contraseña con puntos en lugar de texto visible
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // Mostrar spinner mientras carga, texto normal cuando no
                if (cargando) {
                    CircularProgressIndicator()
                } else {
                    Text("Iniciar sesión")
                }
            }

            Button(
                onClick = {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Crear cuenta")
            }

            if (error.isNotBlank()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}