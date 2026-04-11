package com.example.alltogether.addexpense

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    var titulo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    // Categorías recurrentes (fijas)
    val categorias = listOf(
        5 to "Alquiler",
        6 to "Suministros",
        7 to "Suscripciones"
    )
    var idCategoriaSeleccionada by remember { mutableIntStateOf(5) }
    var expandidoCategoria by remember { mutableStateOf(false) }

    // Selector de frecuencia
    val frecuencias = listOf("DIARIO", "SEMANAL", "MENSUAL", "ANUAL")
    var frecuenciaSeleccionada by remember { mutableStateOf("MENSUAL") }
    var expandidoFrecuencia by remember { mutableStateOf(false) }

    // Día del mes para gastos mensuales
    var diaDelMes by remember { mutableStateOf("1") }

    // Día de la semana para gastos semanales
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

    val nombreCategoriaSeleccionada =
        categorias.firstOrNull { it.first == idCategoriaSeleccionada }?.second ?: "Alquiler"

    val nombreDiaSemanaSeleccionado =
        diasSemana.firstOrNull { it.first == diaSemanaSeleccionado }?.second ?: "Lunes"

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Gasto recurrente",
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
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título del gasto") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad (€)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // Selector de categoría con el mismo formato visual que frecuencia
            ExposedDropdownMenuBox(
                expanded = expandidoCategoria,
                onExpandedChange = { expandidoCategoria = !expandidoCategoria },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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

            // Selector de frecuencia con dropdown
            ExposedDropdownMenuBox(
                expanded = expandidoFrecuencia,
                onExpandedChange = { expandidoFrecuencia = !expandidoFrecuencia },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
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

            // Mostrar campo día del mes solo si es mensual
            if (frecuenciaSeleccionada == "MENSUAL") {
                OutlinedTextField(
                    value = diaDelMes,
                    onValueChange = { diaDelMes = it },
                    label = { Text("Día del mes (1-28)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            // Mostrar selector de día de la semana si es semanal
            if (frecuenciaSeleccionada == "SEMANAL") {
                ExposedDropdownMenuBox(
                    expanded = expandidoDiaSemana,
                    onExpandedChange = { expandidoDiaSemana = !expandidoDiaSemana },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Button(
                onClick = {
                    val cantidadDouble = cantidad.toDoubleOrNull()

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

                    cargando = true
                    mensaje = ""

                    val valorDia = when (frecuenciaSeleccionada) {
                        "MENSUAL" -> diaDelMes.toIntOrNull() ?: 1
                        "SEMANAL" -> diaSemanaSeleccionado
                        else -> 1
                    }

                    coroutineScope.launch {
                        val resultado = withContext(Dispatchers.IO) {
                            service.crearGastoRecurrente(
                                idPareja = idPareja,
                                tituloGasto = titulo,
                                cantidadTotal = cantidadDouble,
                                frecuencia = frecuenciaSeleccionada,
                                diaDelMes = valorDia,
                                idCategoria = idCategoriaSeleccionada,
                                comentario = comentario
                            )
                        }
                        resultado
                            .onSuccess {
                                mensaje = "Gasto recurrente creado correctamente"
                                titulo = ""
                                cantidad = ""
                                comentario = ""
                                diaDelMes = "1"
                                diaSemanaSeleccionado = 1
                                idCategoriaSeleccionada = 5
                                (context as ComponentActivity).finish()
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
                    .padding(top = 16.dp)
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
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}