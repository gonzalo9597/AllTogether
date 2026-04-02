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

@Composable
fun PantallaAddExpense(idPareja: Int) {
    val context = LocalContext.current
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    var titulo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var comentario by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    // Desactiva el botón mientras se procesa la petición
    var cargando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Añadir gasto",
                onBackClick = { (context as androidx.activity.ComponentActivity).finish() }
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

            Button(
                onClick = {
                    // Validar campos antes de enviar
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
                                comentario = comentario
                                // modoReparto es MITAD por defecto
                            )
                        }
                        resultado
                            .onSuccess { gasto ->
                                mensaje = "Gasto guardado — cada uno paga ${gasto.importeUsuario1}€"
                                titulo = ""
                                cantidad = ""
                                comentario = ""
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