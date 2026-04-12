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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.couplesettings.RecurringExpensesActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.selectoricono.SelectorIconoParejaActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FondoPantalla = Color.Black
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)
private val RojoPeligro = Color(0xFFC62828)

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

@OptIn(ExperimentalMaterial3Api::class)
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
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustes pareja",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Ajustes pareja",
                            style = MaterialTheme.typography.headlineSmall,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VerdeSuave),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Personaliza tu pareja",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Cambia el icono, el nombre visible, comparte el código y gestiona tu pareja desde un solo sitio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VerdePrincipal),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Personalización",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clickable {
                                val intent = Intent(context, SelectorIconoParejaActivity::class.java)
                                selectorLauncher.launch(intent)
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = VerdeSuave
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Image(
                            painter = painterResource(id = prefs.nombreIconoAResId(nombreIcono)),
                            contentDescription = "Icono pareja",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = nombreVisible,
                        onValueChange = { nombreVisible = it },
                        label = { Text("Nombre pareja") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sección para compartir el código de invitación con la pareja
                    Text(
                        text = "Invitar a tu pareja",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (cargandoCodigo) {
                            CircularProgressIndicator(
                                color = Color.Black
                            )
                        } else {
                            Text("Generar código de invitación")
                        }
                    }

                    // Muestra el código generado para que el usuario lo comparta
                    if (codigoInvitacion.isNotBlank()) {
                        Text(
                            text = "Código: $codigoInvitacion",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            text = "Comparte este código con tu pareja para que se una",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Gestión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Button(
                        onClick = {
                            val intent = Intent(context, RecurringExpensesActivity::class.java)
                            intent.putExtra("id_pareja", idPareja)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Gastos recurrentes")
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sección para abandonar la pareja
                    Text(
                        text = "Zona de peligro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (!mostrarConfirmacion) {
                        // Primer click — pedir confirmación antes de abandonar
                        Button(
                            onClick = { mostrarConfirmacion = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RojoPeligro,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Abandonar pareja")
                        }
                    } else {
                        // Segunda pantalla — confirmar que realmente quiere abandonar
                        Text(
                            text = "¿Estás seguro? Esta acción no se puede deshacer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
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
                                containerColor = RojoPeligro,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (cargandoAbandonar) {
                                CircularProgressIndicator(
                                    color = Color.Black
                                )
                            } else {
                                Text("Sí, abandonar pareja")
                            }
                        }

                        Button(
                            onClick = { mostrarConfirmacion = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Cancelar")
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            prefs.guardarNombreVisible(idPareja, nombreVisible)
                            prefs.guardarIconoPareja(idPareja, nombreIcono)

                            val activity = context as ComponentActivity
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}