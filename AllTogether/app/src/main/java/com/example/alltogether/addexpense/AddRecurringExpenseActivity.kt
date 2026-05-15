package com.example.alltogether.addexpense

import android.app.Activity
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

private val FondoPantalla = Color(0xFF383A39)
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)
private val GrisTexto = Color(0xFF1F1F1F)

class AddRecurringExpenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val idPareja = intent.getIntExtra("id_pareja", -1)

        setContent {
            AllTogetherTheme {
                PantallaAddRecurringExpense(idPareja)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAddRecurringExpense(idPareja: Int) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { ParejaPreferencesManager(context) }
    val currencyManager = remember { CurrencyPreferencesManager(context) }
    val simboloDivisa = currencyManager.obtenerSimbolo()

    var titulo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    var rolYo by remember { mutableStateOf("USUARIO_1") }

    val categorias = listOf(
        5 to "Alquiler",
        6 to "Suministros",
        7 to "Suscripciones"
    )
    var idCategoriaSeleccionada by remember { mutableIntStateOf(5) }
    var expandidoCategoria by remember { mutableStateOf(false) }

    val frecuencias = listOf("DIARIO", "SEMANAL", "MENSUAL", "ANUAL")
    var frecuenciaSeleccionada by remember { mutableStateOf("MENSUAL") }
    var expandidoFrecuencia by remember { mutableStateOf(false) }

    var diaDelMes by remember { mutableStateOf("1") }

    val diasSemana = listOf(
        1 to "Lunes",
        2 to "Martes",
        3 to "Miércoles",
        4 to "Jueves",
        5 to "Viernes",
        6 to "Sábado",
        7 to "Domingo"
    )
    var diaSemanaSeleccionado by remember { mutableIntStateOf(1) }
    var expandidoDiaSemana by remember { mutableStateOf(false) }

    val porcentajeUsuario1Default = remember(idPareja) {
        prefs.obtenerPorcentajeUsuario1RepartoDefault(idPareja)
    }
    val porcentajeUsuario2Default = remember(idPareja) {
        prefs.obtenerPorcentajeUsuario2RepartoDefault(idPareja)
    }

    var porcentajeYoTexto by remember { mutableStateOf("50") }
    var porcentajeOtroTexto by remember { mutableStateOf("50") }

    var mostrarCardInfo by remember { mutableStateOf(ScreenUiState.mostrarCardInfoAddRecurringExpense) }
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            mostrarCardInfo = false
            ScreenUiState.mostrarCardInfoAddRecurringExpense = false
        }
    }

    LaunchedEffect(idPareja) {
        val resultado = withContext(Dispatchers.IO) {
            service.getBalance(idPareja)
        }

        resultado.onSuccess { balance ->
            rolYo = balance.rolUsuario

            if (rolYo == "USUARIO_1") {
                porcentajeYoTexto = porcentajeUsuario1Default.toString()
                porcentajeOtroTexto = porcentajeUsuario2Default.toString()
            } else {
                porcentajeYoTexto = porcentajeUsuario2Default.toString()
                porcentajeOtroTexto = porcentajeUsuario1Default.toString()
            }
        }
    }

    val nombreCategoriaSeleccionada =
        categorias.firstOrNull { it.first == idCategoriaSeleccionada }?.second ?: "Alquiler"

    val nombreDiaSemanaSeleccionado =
        diasSemana.firstOrNull { it.first == diaSemanaSeleccionado }?.second ?: "Lunes"

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
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = "Gasto recurrente",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Gasto recurrente",
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
                                    text = "Programa un gasto fijo",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = GrisTexto
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Configura importe, categoría, frecuencia y reparto para crear un gasto recurrente",
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
                        text = "Datos del gasto recurrente",
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

                    ExposedDropdownMenuBox(
                        expanded = expandidoCategoria,
                        onExpandedChange = { expandidoCategoria = !expandidoCategoria },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = nombreCategoriaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría", color = Color.Black.copy(alpha = 0.85f)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCategoria)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )

                        ExposedDropdownMenu(
                            expanded = expandidoCategoria,
                            onDismissRequest = { expandidoCategoria = false }
                        ) {
                            categorias.forEach { (idCategoria, nombreCategoria) ->
                                DropdownMenuItem(
                                    text = { Text(nombreCategoria) },
                                    onClick = {
                                        idCategoriaSeleccionada = idCategoria
                                        expandidoCategoria = false
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
                        text = "Frecuencia",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandidoFrecuencia,
                        onExpandedChange = { expandidoFrecuencia = !expandidoFrecuencia },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = frecuenciaSeleccionada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frecuencia", color = Color.Black.copy(alpha = 0.85f)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoFrecuencia)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )

                        ExposedDropdownMenu(
                            expanded = expandidoFrecuencia,
                            onDismissRequest = { expandidoFrecuencia = false }
                        ) {
                            frecuencias.forEach { frecuencia ->
                                DropdownMenuItem(
                                    text = { Text(frecuencia) },
                                    onClick = {
                                        frecuenciaSeleccionada = frecuencia
                                        expandidoFrecuencia = false
                                    }
                                )
                            }
                        }
                    }

                    if (frecuenciaSeleccionada == "MENSUAL") {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = diaDelMes,
                            onValueChange = { diaDelMes = it },
                            label = { Text("Día del mes (1-28)", color = Color.Black.copy(alpha = 0.85f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    if (frecuenciaSeleccionada == "SEMANAL") {
                        Spacer(modifier = Modifier.height(12.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandidoDiaSemana,
                            onExpandedChange = { expandidoDiaSemana = !expandidoDiaSemana },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = nombreDiaSemanaSeleccionado,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Día de la semana", color = Color.Black.copy(alpha = 0.85f)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoDiaSemana)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(16.dp),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                            )

                            ExposedDropdownMenu(
                                expanded = expandidoDiaSemana,
                                onDismissRequest = { expandidoDiaSemana = false }
                            ) {
                                diasSemana.forEach { (numeroDia, nombreDia) ->
                                    DropdownMenuItem(
                                        text = { Text(nombreDia) },
                                        onClick = {
                                            diaSemanaSeleccionado = numeroDia
                                            expandidoDiaSemana = false
                                        }
                                    )
                                }
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = comentario,
                        onValueChange = { comentario = it },
                        label = { Text("Comentario (opcional)", color = Color.Black.copy(alpha = 0.85f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val cantidadDouble = cantidad.toDoubleOrNull()
                            val porcentajeYo = porcentajeYoTexto.toIntOrNull()
                            val porcentajeOtro = porcentajeOtroTexto.toIntOrNull()

                            if (titulo.isBlank()) {
                                mensaje = "Introduce un título"
                                return@Button
                            }

                            if (cantidadDouble == null || cantidadDouble <= 0) {
                                mensaje = "Introduce una cantidad válida"
                                return@Button
                            }

                            if (frecuenciaSeleccionada == "MENSUAL") {
                                val dia = diaDelMes.toIntOrNull()
                                if (dia == null || dia !in 1..28) {
                                    mensaje = "El día del mes debe estar entre 1 y 28"
                                    return@Button
                                }
                            }

                            if (frecuenciaSeleccionada == "SEMANAL" && diaSemanaSeleccionado !in 1..7) {
                                mensaje = "Selecciona un día de la semana válido"
                                return@Button
                            }

                            if (porcentajeYo == null || porcentajeOtro == null) {
                                mensaje = "Introduce un reparto válido"
                                return@Button
                            }

                            if (porcentajeYo !in 0..100 || porcentajeOtro !in 0..100) {
                                mensaje = "Los porcentajes deben estar entre 0 y 100"
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

                            val valorDia = when (frecuenciaSeleccionada) {
                                "MENSUAL" -> diaDelMes.toIntOrNull() ?: 1
                                "SEMANAL" -> diaSemanaSeleccionado
                                else -> 1
                            }

                            cargando = true
                            mensaje = ""

                            coroutineScope.launch {
                                val resultado = withContext(Dispatchers.IO) {
                                    service.crearGastoRecurrente(
                                        idPareja = idPareja,
                                        tituloGasto = titulo,
                                        cantidadTotal = Math.round(currencyManager.aEuros(cantidadDouble) * 100.0) / 100.0,
                                        frecuencia = frecuenciaSeleccionada,
                                        diaDelMes = valorDia,
                                        idCategoria = idCategoriaSeleccionada,
                                        modoReparto = modoRepartoFinal,
                                        comentario = comentario,
                                        porcentajeUsuario1 = porcentajeUsuario1Final.toDouble(),
                                        porcentajeUsuario2 = porcentajeUsuario2Final.toDouble()
                                    )
                                }

                                resultado
                                    .onSuccess {
                                        titulo = ""
                                        cantidad = ""
                                        comentario = ""
                                        diaDelMes = "1"
                                        diaSemanaSeleccionado = 1
                                        idCategoriaSeleccionada = 5

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
                                        mensaje = "No se pudo crear el gasto recurrente"
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
                                text = "Crear gasto recurrente",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (mensaje.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = mensaje,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}