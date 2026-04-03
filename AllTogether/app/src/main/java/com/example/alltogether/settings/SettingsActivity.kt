package com.example.alltogether.settings

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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.login.LoginActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AllTogetherTheme {
                PantallaSettings()
            }
        }
    }
}

@Composable
fun PantallaSettings() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    var divisa by remember { mutableStateOf("EUR") }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }
    var cargandoEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Configuración",
                onBackClick = { (context as ComponentActivity).finish() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = divisa,
                onValueChange = { divisa = it },
                label = { Text("Divisa preferida") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { (context as ComponentActivity).finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Guardar")
            }

            // Botón de cerrar sesión
            Button(
                onClick = {
                    sessionManager.clearSession()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as ComponentActivity).finishAffinity()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Cerrar sesión")
            }

            // Zona de peligro — eliminar cuenta
            Text(
                text = "Zona de peligro",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 24.dp)
            )

            if (!mostrarConfirmacionEliminar) {
                Button(
                    onClick = { mostrarConfirmacionEliminar = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Eliminar cuenta")
                }
            } else {
                Text(
                    text = "¿Estás seguro? Se eliminarán todos tus datos y abandonarás todas tus parejas.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Button(
                    onClick = {
                        cargandoEliminar = true
                        coroutineScope.launch {
                            val resultado = withContext(Dispatchers.IO) {
                                service.eliminarCuenta()
                            }
                            resultado
                                .onSuccess {
                                    // Limpiar sesión y volver a Login
                                    sessionManager.clearSession()
                                    context.startActivity(
                                        Intent(context, LoginActivity::class.java)
                                    )
                                    (context as ComponentActivity).finishAffinity()
                                }
                                .onFailure {
                                    mostrarConfirmacionEliminar = false
                                    cargandoEliminar = false
                                }
                        }
                    },
                    enabled = !cargandoEliminar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (cargandoEliminar) {
                        CircularProgressIndicator()
                    } else {
                        Text("Sí, eliminar mi cuenta")
                    }
                }

                Button(
                    onClick = { mostrarConfirmacionEliminar = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}