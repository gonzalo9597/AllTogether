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

    // Selector de frecuencia
    val frecuencias = listOf("DIARIO", "SEMANAL", "MENSUAL", "ANUAL")
    var frecuenciaSeleccionada by remember { mutableStateOf("MENSUAL") }
    var expandido by remember { mutableStateOf(false) }

    // Día del mes para gastos mensuales
    var diaDelMes by remember { mutableStateOf("1") }

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

            // Selector de frecuencia con dropdown
            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = !expandido },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = frecuenciaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frecuencia") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    frecuencias.forEach { frecuencia ->
                        DropdownMenuItem(
                            text = { Text(frecuencia) },
                            onClick = {
                                frecuenciaSeleccionada = frecuencia
                                expandido = false
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

                    cargando = true
                    mensaje = ""

                    coroutineScope.launch {
                        val resultado = withContext(Dispatchers.IO) {
                            service.crearGastoRecurrente(
                                idPareja = idPareja,
                                tituloGasto = titulo,
                                cantidadTotal = cantidadDouble,
                                frecuencia = frecuenciaSeleccionada,
                                diaDelMes = diaDelMes.toIntOrNull() ?: 1,
                                comentario = comentario
                            )
                        }
                        resultado
                            .onSuccess {
                                mensaje = "Gasto recurrente creado correctamente"
                                titulo = ""
                                cantidad = ""
                                comentario = ""
                                // Volver atrás tras crear
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