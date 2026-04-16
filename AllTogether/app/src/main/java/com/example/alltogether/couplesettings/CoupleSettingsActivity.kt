package com.example.alltogether.couplesettings

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.SwipeToDismissBox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.selectoricono.SelectorIconoParejaActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import com.example.alltogether.util.ScreenUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    var rutaImagenPersonalizada by remember {
        mutableStateOf(prefs.obtenerImagenPersonalizadaPareja(idPareja))
    }

    var rolActual by remember { mutableStateOf("USUARIO_1") }
    var nombreYo by remember { mutableStateOf("Tú") }
    var nombreOtro by remember { mutableStateOf("Tu pareja") }

    var porcentajeYo by remember { mutableStateOf("50") }
    var porcentajeOtro by remember { mutableStateOf("50") }
    var mensajeReparto by remember { mutableStateOf("") }

    var mostrarCardInfo by remember { mutableStateOf(ScreenUiState.mostrarCardInfoCoupleSettings) }
    val dismissState = rememberSwipeToDismissBoxState()

    val selectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val nuevoNombreIcono = result.data?.getStringExtra("icono_nombre")
            val nuevaRutaImagen = result.data?.getStringExtra("imagen_personalizada_path")

            if (nuevoNombreIcono != null) {
                nombreIcono = nuevoNombreIcono
                rutaImagenPersonalizada = null
            }

            if (nuevaRutaImagen != null) {
                rutaImagenPersonalizada = nuevaRutaImagen
            }
        }
    }

    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(idPareja) {
        val porcentajeUsuario1Guardado = prefs.obtenerPorcentajeUsuario1RepartoDefault(idPareja)
        val porcentajeUsuario2Guardado = prefs.obtenerPorcentajeUsuario2RepartoDefault(idPareja)

        val resultadoBalance = withContext(Dispatchers.IO) {
            service.getBalance(idPareja)
        }

        resultadoBalance.onSuccess { balance ->
            rolActual = balance.rolUsuario
            nombreYo = balance.nombreYo
            nombreOtro = balance.nombreOtro
        }

        if (rolActual == "USUARIO_1") {
            porcentajeYo = porcentajeUsuario1Guardado.toString()
            porcentajeOtro = porcentajeUsuario2Guardado.toString()
        } else {
            porcentajeYo = porcentajeUsuario2Guardado.toString()
            porcentajeOtro = porcentajeUsuario1Guardado.toString()
        }
    }

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            mostrarCardInfo = false
            ScreenUiState.mostrarCardInfoCoupleSettings = false
        }
    }

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
                                    text = "Cambia el icono y el nombre visible, ajusta el reparto por defecto, genera un código de invitación, consulta los gastos recurrentes y gestiona tu pareja desde un solo sitio",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                )
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
                        if (rutaImagenPersonalizada != null) {
                            ImagenParejaGuardada(
                                rutaImagen = rutaImagenPersonalizada,
                                fallbackResId = prefs.nombreIconoAResId(nombreIcono),
                                contentDescription = "Imagen pareja",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(12.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = prefs.nombreIconoAResId(nombreIcono)),
                                contentDescription = "Icono pareja",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(12.dp)
                            )
                        }
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

                    Text(
                        text = "Reparto por defecto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Este reparto se usará como valor inicial al crear gastos nuevos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = porcentajeYo,
                            onValueChange = { nuevoValor ->
                                if (nuevoValor.isBlank()) {
                                    porcentajeYo = ""
                                    mensajeReparto = ""
                                } else {
                                    val valor = nuevoValor.toIntOrNull()
                                    if (valor != null && valor in 0..100) {
                                        porcentajeYo = valor.toString()
                                        porcentajeOtro = (100 - valor).toString()
                                        mensajeReparto = ""
                                    }
                                }
                            },
                            label = { Text(nombreYo) },
                            suffix = { Text("%") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = porcentajeOtro,
                            onValueChange = { nuevoValor ->
                                if (nuevoValor.isBlank()) {
                                    porcentajeOtro = ""
                                    mensajeReparto = ""
                                } else {
                                    val valor = nuevoValor.toIntOrNull()
                                    if (valor != null && valor in 0..100) {
                                        porcentajeOtro = valor.toString()
                                        porcentajeYo = (100 - valor).toString()
                                        mensajeReparto = ""
                                    }
                                }
                            },
                            label = { Text(nombreOtro) },
                            suffix = { Text("%") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    if (mensajeReparto.isNotBlank()) {
                        Text(
                            text = mensajeReparto,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

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

                    if (codigoInvitacion.isNotBlank()) {
                        Text(
                            text = "Código: $codigoInvitacion",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            text = "Comparte este código con tu pareja para que se una",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
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

                    Text(
                        text = "Zona de peligro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (!mostrarConfirmacion) {
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
                        Text(
                            text = "¿Estás seguro? Esta acción no se puede deshacer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
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
                            val porcentajeYoInt = porcentajeYo.toIntOrNull()
                            val porcentajeOtroInt = porcentajeOtro.toIntOrNull()

                            if (porcentajeYoInt == null || porcentajeOtroInt == null) {
                                mensajeReparto = "Introduce porcentajes válidos"
                                return@Button
                            }

                            if (porcentajeYoInt !in 0..100 || porcentajeOtroInt !in 0..100) {
                                mensajeReparto = "Los porcentajes deben estar entre 0 y 100"
                                return@Button
                            }

                            if (porcentajeYoInt + porcentajeOtroInt != 100) {
                                mensajeReparto = "Los dos porcentajes deben sumar 100"
                                return@Button
                            }

                            val porcentajeUsuario1 = if (rolActual == "USUARIO_1") {
                                porcentajeYoInt
                            } else {
                                porcentajeOtroInt
                            }

                            val porcentajeUsuario2 = if (rolActual == "USUARIO_1") {
                                porcentajeOtroInt
                            } else {
                                porcentajeYoInt
                            }

                            prefs.guardarNombreVisible(idPareja, nombreVisible)
                            prefs.guardarRepartoPorDefecto(
                                idPareja,
                                porcentajeUsuario1,
                                porcentajeUsuario2
                            )

                            if (rutaImagenPersonalizada != null) {
                                prefs.guardarImagenPersonalizadaPareja(
                                    idPareja,
                                    rutaImagenPersonalizada!!
                                )
                            } else {
                                prefs.guardarIconoPareja(idPareja, nombreIcono)
                            }

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

@Composable
fun ImagenParejaGuardada(
    rutaImagen: String?,
    fallbackResId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(rutaImagen) {
        runCatching {
            if (rutaImagen.isNullOrBlank()) {
                null
            } else {
                BitmapFactory.decodeFile(File(rutaImagen).absolutePath)
            }
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = fallbackResId),
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}