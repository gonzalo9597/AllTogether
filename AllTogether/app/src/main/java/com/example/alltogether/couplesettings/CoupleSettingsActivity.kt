package com.example.alltogether.couplesettings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.selectoricono.SelectorIconoParejaActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import androidx.compose.material3.CircularProgressIndicator
import com.example.alltogether.network.AllTogetherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
class CoupleSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val idPareja = intent.getIntExtra("id_pareja", -1)
        val nombrePareja = intent.getStringExtra("nombre_pareja") ?: "Pareja"




        setContent {
            AllTogetherTheme {
                PantallaCoupleSettings(idPareja, nombrePareja)
            }
        }
    }
}

@Composable
fun PantallaCoupleSettings(idPareja: Int, nombrePareja: String) {
    val context = LocalContext.current
    val prefs = remember { ParejaPreferencesManager(context) }
    var codigoInvitacion by remember { mutableStateOf("") }
    var cargandoCodigo by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var cargandoAbandonar by remember { mutableStateOf(false) }
    var nombreVisible by remember {
        mutableStateOf(prefs.obtenerNombreVisible(idPareja, nombrePareja))
    }

    var nombreIcono by remember {
        mutableStateOf(prefs.obtenerNombreIconoPareja(idPareja))
    }

    val selectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val nuevoNombreIcono =
                result.data?.getStringExtra("icono_nombre") ?: nombreIcono
            nombreIcono = nuevoNombreIcono
        }
    }
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Ajustes pareja",
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
            Text("Icono de la pareja")

            Image(
                painter = painterResource(id = prefs.nombreIconoAResId(nombreIcono)),
                contentDescription = "Icono pareja",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 12.dp)
                    .clickable {
                        val intent = Intent(context, SelectorIconoParejaActivity::class.java)
                        selectorLauncher.launch(intent)
                    }
            )

            OutlinedTextField(
                value = nombreVisible,
                onValueChange = { nombreVisible = it },
                label = { Text("Nombre pareja") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            )
            // Sección para compartir el código de invitación con la pareja
            Text(
                text = "Invitar a tu pareja",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 24.dp)
            )

            Button(
                onClick = {
                    cargandoCodigo = true
                    coroutineScope.launch {
                        val resultado = withContext(Dispatchers.IO) {
                            service.generarCodigoInvitacion(idPareja)
                        }
                        resultado
                            .onSuccess { codigo -> codigoInvitacion = codigo }
                            .onFailure { codigoInvitacion = "Error al generar el código" }
                        cargandoCodigo = false
                    }
                },
                enabled = !cargandoCodigo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (cargandoCodigo) {
                    CircularProgressIndicator()
                } else {
                    Text("Generar código de invitación")
                }
            }

// Muestra el código generado para que el usuario lo comparta
            if (codigoInvitacion.isNotBlank()) {
                Text(
                    text = "Código: $codigoInvitacion",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "Comparte este código con tu pareja para que se una",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            // Sección para abandonar la pareja
            Text(
                text = "Zona de peligro",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 24.dp)
            )

            if (!mostrarConfirmacion) {
                // Primer click — pedir confirmación antes de abandonar
                Button(
                    onClick = { mostrarConfirmacion = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Abandonar pareja")
                }
            } else {
                // Segunda pantalla — confirmar que realmente quiere abandonar
                Text(
                    text = "¿Estás seguro? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = {
                        cargandoAbandonar = true
                        coroutineScope.launch {
                            val resultado = withContext(Dispatchers.IO) {
                                service.abandonarPareja(idPareja)
                            }
                            resultado
                                .onSuccess {
                                    // Volver a MisParejas tras abandonar
                                    val activity = context as ComponentActivity
                                    activity.setResult(Activity.RESULT_OK)
                                    activity.finish()
                                }
                                .onFailure {
                                    mostrarConfirmacion = false
                                    cargandoAbandonar = false
                                }
                        }
                    },
                    enabled = !cargandoAbandonar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (cargandoAbandonar) {
                        CircularProgressIndicator()
                    } else {
                        Text("Sí, abandonar pareja")
                    }
                }

                Button(
                    onClick = { mostrarConfirmacion = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text("Cancelar")
                }
            }
            Button(
                onClick = {
                    prefs.guardarNombreVisible(idPareja, nombreVisible)
                    prefs.guardarIconoPareja(idPareja, nombreIcono)

                    val activity = context as ComponentActivity
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Guardar")
            }
        }
    }
}