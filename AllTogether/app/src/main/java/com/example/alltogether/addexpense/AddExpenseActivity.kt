package com.example.alltogether.addexpense

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

@Composable
fun PantallaAddExpense(idPareja: Int) {
    val context = LocalContext.current
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()
    val activity = context as ComponentActivity

    var titulo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    // Categorías de gastos espontáneos
    val categoriasEspontaneas = listOf(
        1 to "Supermercado",
        2 to "Restaurante",
        3 to "Transporte",
        4 to "Ocio",
        8 to "Otros"
    )
    var idCategoriaSeleccionada by remember { mutableStateOf(4) } // Ocio por defecto
    var desplegableCategoriaAbierto by remember { mutableStateOf(false) }

    // Datos para el selector "Pagado por"
    var nombreYo by remember { mutableStateOf("Tú") }
    var nombreOtro by remember { mutableStateOf("Tu pareja") }
    var rolYo by remember { mutableStateOf("USUARIO_1") }
    var pagadoPor by remember { mutableStateOf("USUARIO_1") }
    var desplegablePagadoPorAbierto by remember { mutableStateOf(false) }

    // Fecha por defecto = hoy
    val calendario = remember { Calendar.getInstance() }
    var fechaGasto by remember { mutableStateOf(formatearFecha(calendario)) }

    LaunchedEffect(idPareja) {
        val resultado = withContext(Dispatchers.IO) {
            service.getBalance(idPareja)
        }

        resultado.onSuccess { balance ->
            nombreYo = balance.nombreYo
            nombreOtro = balance.nombreOtro
            rolYo = balance.rolUsuario
            pagadoPor = balance.rolUsuario
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
        topBar = {
            TopAppBarWithBack(
                title = "Añadir gasto",
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

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario (opcional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Categoría",
                    modifier = Modifier.width(120.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { desplegableCategoriaAbierto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(textoCategoria)
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
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pagado por",
                    modifier = Modifier.width(120.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { desplegablePagadoPorAbierto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(textoPagadoPor)
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
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cuando",
                    modifier = Modifier.width(120.dp)
                )

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
                    modifier = Modifier.weight(1f)
                ) {
                    Text(fechaGasto)
                }
            }

            Button(
                onClick = {
                    val cantidadDouble = cantidad.toDoubleOrNull()

                    if (titulo.isBlank()) {
                        mensaje = "Introduce un título para el gasto"
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
                            service.guardarGasto(
                                idPareja = idPareja,
                                tituloGasto = titulo,
                                cantidadTotal = cantidadDouble,
                                comentario = comentario,
                                fechaGasto = fechaGasto,
                                pagadoPor = pagadoPor,
                                idCategoria = idCategoriaSeleccionada
                            )
                        }

                        resultado
                            .onSuccess { gasto ->
                                mensaje = "Gasto guardado — cada uno paga ${gasto.importeUsuario1}€"
                                titulo = ""
                                cantidad = ""
                                comentario = ""
                                fechaGasto = formatearFecha(Calendar.getInstance())
                                idCategoriaSeleccionada = 4
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator()
                } else {
                    Text("Guardar gasto")
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