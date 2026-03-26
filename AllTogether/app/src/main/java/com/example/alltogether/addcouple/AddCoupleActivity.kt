package com.example.alltogether.addcouple

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCoupleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AllTogetherTheme {
                PantallaAddCouple()
            }
        }
    }
}

@Composable
fun PantallaAddCouple() {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    var nombrePareja by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Añadir pareja",
                onBackClick = { activity.finish() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Crear nueva pareja",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = nombrePareja,
                onValueChange = { nombrePareja = it },
                label = { Text("Nombre de la pareja") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Button(
                onClick = {
                    if (nombrePareja.isBlank()) {
                        mensaje = "Introduce un nombre para la pareja"
                    } else {
                        coroutineScope.launch {
                            val ok = withContext(Dispatchers.IO) {
                                service.crearPareja(nombrePareja)
                            }

                            if (ok) {
                                Toast.makeText(
                                    context,
                                    "Pareja creada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                mensaje = "Pareja creada correctamente"
                                nombrePareja = ""
                                activity.setResult(Activity.RESULT_OK)
                            } else {
                                mensaje = "No se pudo crear la pareja"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Crear pareja")
            }

            Text(
                text = "O unirse con código",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp)
            )

            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código de invitación") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            service.unirseConCodigo(codigo)
                        }

                        if (ok) {
                            Toast.makeText(
                                context,
                                "Te has unido a la pareja correctamente",
                                Toast.LENGTH_SHORT
                            ).show()

                            mensaje = "Te has unido a la pareja correctamente"
                            codigo = ""
                            activity.setResult(Activity.RESULT_OK)
                        } else {
                            mensaje = "Código no válido"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("Unirse")
            }

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
