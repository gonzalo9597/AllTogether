package com.example.alltogether.addexpense

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.CurrencyPreferencesManager
import com.example.alltogether.util.ParejaPreferencesManager
import com.example.alltogether.util.ScreenUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val FondoPantalla = Color(0xFF383A39)
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)
private val GrisTexto = Color(0xFF1F1F1F)

class AddExpenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val idPareja = intent.getIntExtra("id_pareja", -1)

        setContent {
            AllTogetherTheme {
                PantallaAddExpense(idPareja)
            }
        }
    }
}

private fun formatearFecha(calendario: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendario.time)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAddExpense(idPareja: Int) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()
    val parejaPrefs = remember { ParejaPreferencesManager(context) }
    val currencyManager = remember { CurrencyPreferencesManager(context) }
    val simboloDivisa = currencyManager.obtenerSimbolo()

    var titulo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val porcentajeUsuario1Default = remember(idPareja) {
        parejaPrefs.obtenerPorcentajeUsuario1RepartoDefault(idPareja)
    }
    val porcentajeUsuario2Default = remember(idPareja) {
        parejaPrefs.obtenerPorcentajeUsuario2RepartoDefault(idPareja)
    }

    var porcentajeYoTexto by remember { mutableStateOf("50") }
    var porcentajeOtroTexto by remember { mutableStateOf("50") }

    val categoriasEspontaneas = listOf(
        1 to "Supermercado",
        2 to "Restaurante",
        3 to "Transporte",
        4 to "Ocio",
        8 to "Otros"
    )
    var idCategoriaSeleccionada by remember { mutableStateOf(4) }
    var desplegableCategoriaAbierto by remember { mutableStateOf(false) }

    var nombreYo by remember { mutableStateOf("Tú") }
    var nombreOtro by remember { mutableStateOf("Tu pareja") }
    var rolYo by remember { mutableStateOf("USUARIO_1") }
    var pagadoPor by remember { mutableStateOf("USUARIO_1") }
    var desplegablePagadoPorAbierto by remember { mutableStateOf(false) }

    val calendario = remember { Calendar.getInstance() }
    var fechaGasto by remember { mutableStateOf(formatearFecha(calendario)) }

    var mostrarCardInfo by remember { mutableStateOf(ScreenUiState.mostrarCardInfoAddExpense) }
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            mostrarCardInfo = false
            ScreenUiState.mostrarCardInfoAddExpense = false
        }
    }

    LaunchedEffect(idPareja) {
        val resultado = withContext(Dispatchers.IO) {
            service.getBalance(idPareja)
        }

        resultado.onSuccess { balance ->
            nombreYo = balance.nombreYo
            nombreOtro = balance.nombreOtro
            rolYo = balance.rolUsuario
            pagadoPor = balance.rolUsuario

            if (rolYo == "USUARIO_1") {
                porcentajeYoTexto = porcentajeUsuario1Default.toString()
                porcentajeOtroTexto = porcentajeUsuario2Default.toString()
            } else {
                porcentajeYoTexto = porcentajeUsuario2Default.toString()
                porcentajeOtroTexto = porcentajeUsuario1Default.toString()
            }
        }
    }

    val rolOtro = if (rolYo == "USUARIO_1") "USUARIO_2" else "USUARIO_1"

    val textoPagadoPor = when (pagadoPor) {
        rolYo -> nombreYo
        rolOtro -> nombreOtro
        else -> nombreYo
    }

    val textoCategoria = categoriasEspontaneas
        .firstOrNull { it.first == idCategoriaSeleccionada }
        ?.second ?: "Ocio"

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = "Añadir gasto",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Añadir gasto",
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
                            colors = CardDefaults.cardColors(containerColor = VerdeSuave),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Text(
                                    text = "Registra un nuevo gasto",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GrisTexto
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Añade el importe, quién lo pagó, la categoría, la fecha y el reparto del gasto",
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
                colors = CardDefaults.cardColors(containerColor = VerdePrincipal),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Datos del gasto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título del gasto", color = Color.Black.copy(alpha = 0.85f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad ($simboloDivisa)", color = Color.Black.copy(alpha = 0.85f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        label = { Text("Comentario (opcional)", color = Color.Black.copy(alpha = 0.85f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { desplegableCategoriaAbierto = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = textoCategoria, fontWeight = FontWeight.SemiBold)
                        }

                        DropdownMenu(
                            expanded = desplegableCategoriaAbierto,
                            onDismissRequest = { desplegableCategoriaAbierto = false }
                        ) {
                            categoriasEspontaneas.forEach { (idCategoria, nombreCategoria) ->
                                DropdownMenuItem(
                                    text = { Text(nombreCategoria) },
                                    onClick = {
                                        idCategoriaSeleccionada = idCategoria
                                        desplegableCategoriaAbierto = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Pagado por",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { desplegablePagadoPorAbierto = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = VerdeSuave,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = textoPagadoPor, fontWeight = FontWeight.SemiBold)
                        }

                        DropdownMenu(
                            expanded = desplegablePagadoPorAbierto,
                            onDismissRequest = { desplegablePagadoPorAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(nombreYo) },
                                onClick = {
                                    pagadoPor = rolYo
                                    desplegablePagadoPorAbierto = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(nombreOtro) },
                                onClick = {
                                    pagadoPor = rolOtro
                                    desplegablePagadoPorAbierto = false
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Reparto del gasto",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = porcentajeYoTexto,
                            onValueChange = { nuevoValor ->
                                val numero = nuevoValor.toIntOrNull()
                                if (nuevoValor.isEmpty()) {
                                    porcentajeYoTexto = ""
                                    porcentajeOtroTexto = ""
                                } else if (numero != null && numero in 0..100) {
                                    porcentajeYoTexto = numero.toString()
                                    porcentajeOtroTexto = (100 - numero).toString()
                                }
                            },
                            label = { Text("Tú (%)", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = porcentajeOtroTexto,
                            onValueChange = { nuevoValor ->
                                val numero = nuevoValor.toIntOrNull()
                                if (nuevoValor.isEmpty()) {
                                    porcentajeYoTexto = ""
                                    porcentajeOtroTexto = ""
                                } else if (numero != null && numero in 0..100) {
                                    porcentajeOtroTexto = numero.toString()
                                    porcentajeYoTexto = (100 - numero).toString()
                                }
                            },
                            label = { Text("Tu pareja (%)", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Cuando",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendario.set(Calendar.YEAR, year)
                                    calendario.set(Calendar.MONTH, month)
                                    calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    fechaGasto = formatearFecha(calendario)
                                },
                                calendario.get(Calendar.YEAR),
                                calendario.get(Calendar.MONTH),
                                calendario.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = fechaGasto, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val cantidadDouble = cantidad.toDoubleOrNull()
                            val porcentajeYo = porcentajeYoTexto.toIntOrNull()
                            val porcentajeOtro = porcentajeOtroTexto.toIntOrNull()

                            if (titulo.isBlank()) {
                                mensaje = "Introduce un título para el gasto"
                                return@Button
                            }

                            if (cantidadDouble == null || cantidadDouble <= 0) {
                                mensaje = "Introduce una cantidad válida"
                                return@Button
                            }

                            if (porcentajeYo == null || porcentajeOtro == null) {
                                mensaje = "Introduce un reparto válido"
                                return@Button
                            }

                            if (porcentajeYo < 0 || porcentajeOtro < 0) {
                                mensaje = "Los porcentajes no pueden ser negativos"
                                return@Button
                            }

                            if (porcentajeYo + porcentajeOtro != 100) {
                                mensaje = "Los porcentajes deben sumar 100"
                                return@Button
                            }

                            val porcentajeUsuario1Final = if (rolYo == "USUARIO_1") {
                                porcentajeYo
                            } else {
                                porcentajeOtro
                            }

                            val porcentajeUsuario2Final = if (rolYo == "USUARIO_1") {
                                porcentajeOtro
                            } else {
                                porcentajeYo
                            }

                            val modoRepartoFinal =
                                if (porcentajeUsuario1Final == 50 && porcentajeUsuario2Final == 50) {
                                    "MITAD"
                                } else {
                                    "PORCENTAJE"
                                }

                            cargando = true
                            mensaje = ""

                            coroutineScope.launch {
                                val resultado = withContext(Dispatchers.IO) {
                                    service.guardarGasto(
                                        idPareja = idPareja,
                                        tituloGasto = titulo,
                                        cantidadTotal = cantidadDouble,
                                        modoReparto = modoRepartoFinal,
                                        comentario = comentario,
                                        fechaGasto = fechaGasto,
                                        pagadoPor = pagadoPor,
                                        idCategoria = idCategoriaSeleccionada,
                                        porcentajeUsuario1 = porcentajeUsuario1Final.toDouble(),
                                        porcentajeUsuario2 = porcentajeUsuario2Final.toDouble()
                                    )
                                }

                                resultado
                                    .onSuccess { gasto ->
                                        val miImporte = if (rolYo == "USUARIO_1") {
                                            gasto.importeUsuario1
                                        } else {
                                            gasto.importeUsuario2
                                        }

                                        mensaje = "Gasto guardado — tu parte es ${"%.2f".format(miImporte)}$simboloDivisa"
                                        titulo = ""
                                        cantidad = ""
                                        comentario = ""
                                        fechaGasto = formatearFecha(Calendar.getInstance())
                                        idCategoriaSeleccionada = 4

                                        if (rolYo == "USUARIO_1") {
                                            porcentajeYoTexto = porcentajeUsuario1Default.toString()
                                            porcentajeOtroTexto = porcentajeUsuario2Default.toString()
                                        } else {
                                            porcentajeYoTexto = porcentajeUsuario2Default.toString()
                                            porcentajeOtroTexto = porcentajeUsuario1Default.toString()
                                        }

                                        activity.setResult(Activity.RESULT_OK)
                                        activity.finish()
                                    }
                                    .onFailure {
                                        mensaje = "No se pudo guardar el gasto"
                                    }

                                cargando = false
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
                                text = "Guardar gasto",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (mensaje.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}