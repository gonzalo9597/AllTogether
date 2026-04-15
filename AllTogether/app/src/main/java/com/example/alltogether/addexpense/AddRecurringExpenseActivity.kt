package com.example.alltogether.addexpense

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        topBar = {
            TopAppBarWithBack(
                title = "Gasto recurrente",
                onBackClick = { activity.finish() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del gasto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad (€)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            ExposedDropdownMenuBox(
                expanded = expandidoCategoria,
                onExpandedChange = { expandidoCategoria = !expandidoCategoria },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nombreCategoriaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoCategoria)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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

            ExposedDropdownMenuBox(
                expanded = expandidoFrecuencia,
                onExpandedChange = { expandidoFrecuencia = !expandidoFrecuencia },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = frecuenciaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frecuencia") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoFrecuencia)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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
                OutlinedTextField(
                    value = diaDelMes,
                    onValueChange = { diaDelMes = it },
                    label = { Text("Día del mes (1-28)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            if (frecuenciaSeleccionada == "SEMANAL") {
                ExposedDropdownMenuBox(
                    expanded = expandidoDiaSemana,
                    onExpandedChange = { expandidoDiaSemana = !expandidoDiaSemana },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = nombreDiaSemanaSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día de la semana") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoDiaSemana)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
                label = { Text("Tu porcentaje") },
                modifier = Modifier.fillMaxWidth(),
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
                label = { Text("Porcentaje de tu pareja") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

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
                                cantidadTotal = cantidadDouble,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator()
                } else {
                    Text("Crear gasto recurrente")
                }
            }

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}