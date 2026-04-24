package com.example.alltogether.editarGasto

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.dashboard.RojoPeligro
import com.example.alltogether.dashboard.VerdePrincipal
import com.example.alltogether.dashboard.VerdeSuave
import com.example.alltogether.model.Gasto
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.CurrencyPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val FondoPantalla = Color(0xFF383A39)

class ExpenseDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        @Suppress("DEPRECATION")
        val gasto = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("gasto", Gasto::class.java)
        } else {
            intent.getSerializableExtra("gasto") as? Gasto
        }

        val rolUsuario = intent.getStringExtra("rol_usuario") ?: "USUARIO_1"

        if (gasto == null) {
            finish()
            return
        }

        setContent {
            AllTogetherTheme {
                PantallaDetalleGasto(
                    gasto = gasto,
                    rolUsuario = rolUsuario,
                    onBack = { finish() },
                    onActionDone = {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleGasto(
    gasto: Gasto,
    rolUsuario: String,
    onBack: () -> Unit,
    onActionDone: () -> Unit
) {
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val currencyManager = remember { CurrencyPreferencesManager(context) }
    val simboloDivisa = remember { currencyManager.obtenerSimbolo() }

    var mostrarEditar by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }

    val yoPague = if (rolUsuario == "USUARIO_1") gasto.pagadoUsuario1 else gasto.pagadoUsuario2
    val otroPago = if (rolUsuario == "USUARIO_1") gasto.pagadoUsuario2 else gasto.pagadoUsuario1
    val rolOtro = if (rolUsuario == "USUARIO_1") "USUARIO_2" else "USUARIO_1"

    val importeYo = if (rolUsuario == "USUARIO_1") gasto.importeUsuario1 else gasto.importeUsuario2
    val importeOtro = if (rolUsuario == "USUARIO_1") gasto.importeUsuario2 else gasto.importeUsuario1

    val porcentajeYo = if (rolUsuario == "USUARIO_1") gasto.porcentajeUsuario1 else gasto.porcentajeUsuario2
    val porcentajeOtro = if (rolUsuario == "USUARIO_1") gasto.porcentajeUsuario2 else gasto.porcentajeUsuario1

    if (mostrarEditar) {
        EditarGastoDialogDetalle(
            gasto = gasto,
            rolUsuario = rolUsuario,
            simboloDivisa = simboloDivisa,
            onConfirmar = { titulo, cantidad, comentario, modoReparto, porcentajeUsuario1, porcentajeUsuario2 ->
                cargando = true
                coroutineScope.launch {
                    val resultado = withContext(Dispatchers.IO) {
                        service.editarGasto(
                            idGasto = gasto.idGasto,
                            tituloGasto = titulo,
                            cantidadTotal = cantidad,
                            comentario = comentario,
                            idCategoria = gasto.idCategoria,
                            modoReparto = modoReparto,
                            porcentajeUsuario1 = porcentajeUsuario1,
                            porcentajeUsuario2 = porcentajeUsuario2
                        )
                    }

                    if (resultado.isSuccess) {
                        onActionDone()
                    } else {
                        mensaje = "No se pudo editar el gasto"
                    }

                    cargando = false
                    mostrarEditar = false
                }
            },
            onCancelar = { mostrarEditar = false }
        )
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                            contentDescription = "Detalle del gasto",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Detalle del gasto",
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VerdePrincipal),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = gasto.tituloGasto,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${"%.2f".format(currencyManager.convertir(gasto.cantidadTotal))}$simboloDivisa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Fecha: ${gasto.fechaGasto}",
                        color = Color.White
                    )

                    Text(
                        text = "Categoría: ${gasto.nombreCategoria}",
                        modifier = Modifier.padding(top = 6.dp),
                        color = Color.White
                    )

                    Text(
                        text = "Modo reparto: ${gasto.modoReparto}",
                        modifier = Modifier.padding(top = 6.dp),
                        color = Color.White
                    )

                    if (importeYo != null && importeOtro != null) {
                        Text(
                            text = "Reparto: tú ${"%.2f".format(currencyManager.convertir(importeYo))}$simboloDivisa / tu pareja ${"%.2f".format(currencyManager.convertir(importeOtro))}$simboloDivisa",
                            modifier = Modifier.padding(top = 6.dp),
                            color = Color.White
                        )
                    }

                    if (porcentajeYo != null && porcentajeOtro != null) {
                        Text(
                            text = "Porcentajes: tú ${"%.0f".format(porcentajeYo)}% / tu pareja ${"%.0f".format(porcentajeOtro)}%",
                            modifier = Modifier.padding(top = 6.dp),
                            color = Color.White
                        )
                    }

                    if (gasto.comentario.isNotBlank()) {
                        Text(
                            text = "Comentario: ${gasto.comentario}",
                            modifier = Modifier.padding(top = 6.dp),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = VerdeSuave.copy(alpha = 0.55f)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (gasto.esPendiente) "Estado: Pendiente" else "Estado: Saldado",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Text(
                        text = if (yoPague) "✅ Tú ya pagaste tu parte" else "⏳ Tú aún no has pagado",
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color.White
                    )

                    Text(
                        text = if (otroPago) "✅ Tu pareja ya pagó su parte" else "⏳ Tu pareja aún no ha pagado",
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color.White
                    )
                }
            }

            if (gasto.esPendiente && !yoPague) {
                Button(
                    onClick = {
                        cargando = true
                        coroutineScope.launch {
                            val resultado = withContext(Dispatchers.IO) {
                                service.saldarDeuda(gasto.idGasto, rolUsuario)
                            }

                            if (resultado.isSuccess) {
                                onActionDone()
                            } else {
                                mensaje = "No se pudo marcar tu parte como pagada"
                            }

                            cargando = false
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Marcar mi parte como pagada",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (gasto.esPendiente && !otroPago) {
                Button(
                    onClick = {
                        cargando = true
                        coroutineScope.launch {
                            val resultado = withContext(Dispatchers.IO) {
                                service.saldarDeuda(gasto.idGasto, rolOtro)
                            }

                            if (resultado.isSuccess) {
                                onActionDone()
                            } else {
                                mensaje = "No se pudo marcar su parte como pagada"
                            }

                            cargando = false
                        }
                    },
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Marcar su parte como pagada",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Button(
                onClick = { mostrarEditar = true },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeSuave,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Editar",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    cargando = true
                    coroutineScope.launch {
                        val resultado = withContext(Dispatchers.IO) {
                            service.eliminarGasto(gasto.idGasto)
                        }

                        if (resultado.isSuccess) {
                            onActionDone()
                        } else {
                            mensaje = "No se pudo eliminar el gasto"
                        }

                        cargando = false
                    }
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RojoPeligro,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Eliminar",
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun EditarGastoDialogDetalle(
    gasto: Gasto,
    rolUsuario: String,
    simboloDivisa: String,
    onConfirmar: (String, Double, String, String, Double?, Double?) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf(gasto.tituloGasto) }
    var cantidad by remember { mutableStateOf(gasto.cantidadTotal.toString()) }
    var comentario by remember { mutableStateOf(gasto.comentario) }

    val porcentajeInicialYo = if (rolUsuario == "USUARIO_1") {
        gasto.porcentajeUsuario1?.toInt() ?: 50
    } else {
        gasto.porcentajeUsuario2?.toInt() ?: 50
    }

    val porcentajeInicialOtro = if (rolUsuario == "USUARIO_1") {
        gasto.porcentajeUsuario2?.toInt() ?: 50
    } else {
        gasto.porcentajeUsuario1?.toInt() ?: 50
    }

    var porcentajeYoTexto by remember { mutableStateOf(porcentajeInicialYo.toString()) }
    var porcentajeOtroTexto by remember { mutableStateOf(porcentajeInicialOtro.toString()) }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = VerdePrincipal,
        title = {
            Text(
                text = "Editar gasto",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.65f),
                        cursorColor = Color.Black
                    )
                )

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad ($simboloDivisa)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.65f),
                        cursorColor = Color.Black
                    )
                )

                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.65f),
                        cursorColor = Color.Black
                    )
                )

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
                        label = { Text("Tú (%)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.65f),
                            cursorColor = Color.Black
                        )
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
                        label = { Text("Pareja (%)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.65f),
                            cursorColor = Color.Black
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cantidadDouble = cantidad.toDoubleOrNull()
                    val porcentajeYo = porcentajeYoTexto.toIntOrNull()
                    val porcentajeOtro = porcentajeOtroTexto.toIntOrNull()

                    if (titulo.isBlank() || cantidadDouble == null || cantidadDouble <= 0) {
                        return@Button
                    }

                    if (porcentajeYo == null || porcentajeOtro == null) {
                        return@Button
                    }

                    if (porcentajeYo + porcentajeOtro != 100) {
                        return@Button
                    }

                    val porcentajeUsuario1Final = if (rolUsuario == "USUARIO_1") {
                        porcentajeYo.toDouble()
                    } else {
                        porcentajeOtro.toDouble()
                    }

                    val porcentajeUsuario2Final = if (rolUsuario == "USUARIO_1") {
                        porcentajeOtro.toDouble()
                    } else {
                        porcentajeYo.toDouble()
                    }

                    val modoRepartoFinal =
                        if (porcentajeUsuario1Final == 50.0 && porcentajeUsuario2Final == 50.0) {
                            "MITAD"
                        } else {
                            "PORCENTAJE"
                        }

                    onConfirmar(
                        titulo,
                        cantidadDouble,
                        comentario,
                        modoRepartoFinal,
                        porcentajeUsuario1Final,
                        porcentajeUsuario2Final
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeSuave,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Guardar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onCancelar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeSuave,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}