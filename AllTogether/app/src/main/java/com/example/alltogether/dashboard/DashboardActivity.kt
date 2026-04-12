package com.example.alltogether.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alltogether.addexpense.AddExpenseActivity
import com.example.alltogether.addexpense.AddRecurringExpenseActivity
import com.example.alltogether.couplesettings.CoupleSettingsActivity
import com.example.alltogether.editarGasto.ExpenseDetailActivity
import com.example.alltogether.model.Gasto
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment

val FondoPantalla = Color.Black
val VerdePrincipal = Color(0xFF2DBC94)
val VerdeSuave = Color(0xFFA6E6DB)
val RojoPeligro = Color(0xFFC62828)

class DashboardActivity : ComponentActivity() {

    private var idPareja: Int = -1
    private var nombreParejaOriginal: String = "Pareja"
    private lateinit var parejaPrefs: ParejaPreferencesManager
    private var huboCambios by mutableStateOf(false)
    private var nombreVisible by mutableStateOf("Pareja")
    private var recargarGastos by mutableStateOf(0)

    private val ajustesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cargarDatosPantalla()
                huboCambios = true
                finish()
            }
        }

    private val addExpenseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                recargarGastos++
            }
        }

    private val detalleGastoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                recargarGastos++
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        idPareja = intent.getIntExtra("id_pareja", -1)
        nombreParejaOriginal = intent.getStringExtra("nombre_pareja") ?: "Pareja"
        parejaPrefs = ParejaPreferencesManager(this)
        cargarDatosPantalla()

        setContent {
            AllTogetherTheme {
                PantallaDashboard(
                    nombrePareja = nombreVisible,
                    idPareja = idPareja,
                    recargar = recargarGastos,
                    onBackClick = { finish() },
                    onSettingsClick = { abrirAjustesPareja() },
                    onAddExpenseClick = { abrirAnadirGasto() },
                    onOpenExpenseDetail = { gasto, rolUsuario ->
                        abrirDetalleGasto(gasto, rolUsuario)
                    }
                )
            }
        }
    }

    private fun abrirAjustesPareja() {
        val intent = Intent(this, CoupleSettingsActivity::class.java)
        intent.putExtra("id_pareja", idPareja)
        intent.putExtra("nombre_pareja", nombreParejaOriginal)
        ajustesLauncher.launch(intent)
    }

    private fun abrirAnadirGasto() {
        val intent = Intent(this, AddExpenseActivity::class.java)
        intent.putExtra("id_pareja", idPareja)
        addExpenseLauncher.launch(intent)
    }

    private fun abrirDetalleGasto(gasto: Gasto, rolUsuario: String) {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        intent.putExtra("gasto", gasto)
        intent.putExtra("rol_usuario", rolUsuario)
        detalleGastoLauncher.launch(intent)
    }

    private fun cargarDatosPantalla() {
        nombreVisible = parejaPrefs.obtenerNombreVisible(idPareja, nombreParejaOriginal)
    }

    override fun finish() {
        if (huboCambios) setResult(Activity.RESULT_OK)
        super.finish()
    }
}

private fun formatearCabeceraFecha(fechaIso: String): String {
    return try {
        val partes = fechaIso.split("-")
        "${partes[2]}/${partes[1]}"
    } catch (e: Exception) {
        fechaIso
    }
}

data class ResumenDashboard(
    val deudaYoAOtro: Double,
    val deudaOtroAMi: Double,
    val pendienteGeneralYo: Double,
    val pendienteGeneralOtro: Double
)

private fun calcularResumenDashboard(gastos: List<Gasto>, rolActual: String): ResumenDashboard {
    var deudaYoAOtro = 0.0
    var deudaOtroAMi = 0.0
    var pendienteGeneralYo = 0.0
    var pendienteGeneralOtro = 0.0

    gastos.filter { it.esPendiente }.forEach { gasto ->
        val yoPague = if (rolActual == "USUARIO_1") gasto.pagadoUsuario1 else gasto.pagadoUsuario2
        val otroPago = if (rolActual == "USUARIO_1") gasto.pagadoUsuario2 else gasto.pagadoUsuario1

        val importeYo = if (rolActual == "USUARIO_1") {
            gasto.importeUsuario1 ?: 0.0
        } else {
            gasto.importeUsuario2 ?: 0.0
        }

        val importeOtro = if (rolActual == "USUARIO_1") {
            gasto.importeUsuario2 ?: 0.0
        } else {
            gasto.importeUsuario1 ?: 0.0
        }

        when {
            !yoPague && otroPago -> deudaYoAOtro += importeYo
            yoPague && !otroPago -> deudaOtroAMi += importeOtro
            !yoPague && !otroPago -> {
                pendienteGeneralYo += importeYo
                pendienteGeneralOtro += importeOtro
            }
        }
    }

    return ResumenDashboard(
        deudaYoAOtro = deudaYoAOtro,
        deudaOtroAMi = deudaOtroAMi,
        pendienteGeneralYo = pendienteGeneralYo,
        pendienteGeneralOtro = pendienteGeneralOtro
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDashboard(
    nombrePareja: String,
    idPareja: Int,
    recargar: Int,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onOpenExpenseDetail: (Gasto, String) -> Unit
) {
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    var gastos by remember { mutableStateOf<List<Gasto>>(emptyList()) }
    var recargarInterno by remember { mutableStateOf(recargar) }
    var balance by remember { mutableStateOf<AllTogetherService.Balance?>(null) }

    var filtroEstado by remember { mutableStateOf("TODOS") }
    var filtroFecha by remember { mutableStateOf("TODOS") }
    var filtroOrden by remember { mutableStateOf("FECHA_DESC") }
    var filtroCategoria by remember { mutableStateOf("TODAS") }
    var mostrarFiltros by remember { mutableStateOf(false) }
    var desplegableCategoriaAbierto by remember { mutableStateOf(false) }

    val categoriasFiltro = listOf(
        "TODAS" to "Todas",
        "Supermercado" to "Supermercado",
        "Restaurante" to "Restaurante",
        "Transporte" to "Transporte",
        "Ocio" to "Ocio",
        "Alquiler" to "Alquiler",
        "Suministros" to "Suministros",
        "Suscripciones" to "Suscripciones",
        "Otros" to "Otros"
    )

    val textoFiltroCategoria = categoriasFiltro
        .firstOrNull { it.first == filtroCategoria }
        ?.second ?: "Todas"

    LaunchedEffect(recargar, recargarInterno) {
        gastos = withContext(Dispatchers.IO) { service.getGastos(idPareja) }
        val balanceResult = withContext(Dispatchers.IO) { service.getBalance(idPareja) }
        balanceResult.onSuccess { balance = it }
    }

    val gastosFiltrados = gastos
        .filter { gasto ->
            when (filtroEstado) {
                "PENDIENTE" -> gasto.esPendiente
                "SALDADO" -> !gasto.esPendiente
                else -> true
            }
        }
        .filter { gasto ->
            when (filtroFecha) {
                "ESTE_MES" -> {
                    val cal = java.util.Calendar.getInstance()
                    val anyo = cal.get(java.util.Calendar.YEAR)
                    val mes = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                    gasto.fechaGasto.startsWith("$anyo-$mes")
                }
                "MES_ANTERIOR" -> {
                    val cal = java.util.Calendar.getInstance()
                    cal.add(java.util.Calendar.MONTH, -1)
                    val anyo = cal.get(java.util.Calendar.YEAR)
                    val mes = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                    gasto.fechaGasto.startsWith("$anyo-$mes")
                }
                else -> true
            }
        }
        .filter { gasto ->
            filtroCategoria == "TODAS" || gasto.nombreCategoria == filtroCategoria
        }
        .sortedWith(
            when (filtroOrden) {
                "IMPORTE_DESC" -> compareByDescending<Gasto> { it.cantidadTotal }
                "IMPORTE_ASC" -> compareBy<Gasto> { it.cantidadTotal }
                else -> compareByDescending<Gasto> { it.fechaGasto }
            }
        )

    val gastosAgrupados = gastosFiltrados.groupBy { it.fechaGasto }
    val rolActual = balance?.rolUsuario ?: "USUARIO_1"
    val rolOtro = if (rolActual == "USUARIO_1") "USUARIO_2" else "USUARIO_1"
    val nombreOtro = balance?.nombreOtro ?: "Tu pareja"

    val resumen = calcularResumenDashboard(gastos, rolActual)
    val deudaNeta = resumen.deudaYoAOtro - resumen.deudaOtroAMi
    val estanAlDia = gastos.isNotEmpty() &&
            kotlin.math.abs(deudaNeta) <= 0.009 &&
            resumen.pendienteGeneralYo <= 0.009 &&
            resumen.pendienteGeneralOtro <= 0.009

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = nombrePareja,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = Color.White
                        )

                        if (estanAlDia) {
                            Text(
                                text = "¡Estáis al día! 🎉",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustes pareja",
                            modifier = Modifier.size(28.dp),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoPantalla)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val context = androidx.compose.ui.platform.LocalContext.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddExpenseClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = VerdePrincipal
                        )
                        Text(
                            text = "  Añadir gasto",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(context, AddRecurringExpenseActivity::class.java)
                            intent.putExtra("id_pareja", idPareja)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeSuave,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = VerdePrincipal
                        )
                        Text(
                            text = "  Gasto recurrente",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { mostrarFiltros = !mostrarFiltros },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(if (mostrarFiltros) "  Ocultar filtros" else "  Filtros")
                }

                if (mostrarFiltros) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = VerdePrincipal),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Estado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                listOf(
                                    "TODOS" to "Todos",
                                    "PENDIENTE" to "Pendiente",
                                    "SALDADO" to "Saldado"
                                ).forEach { (valor, etiqueta) ->
                                    FilterChip(
                                        selected = filtroEstado == valor,
                                        onClick = { filtroEstado = valor },
                                        label = { Text(etiqueta) }
                                    )
                                }
                            }

                            Text(
                                "Fecha",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                listOf(
                                    "TODOS" to "Todos",
                                    "ESTE_MES" to "Este mes",
                                    "MES_ANTERIOR" to "Mes anterior"
                                ).forEach { (valor, etiqueta) ->
                                    FilterChip(
                                        selected = filtroFecha == valor,
                                        onClick = { filtroFecha = valor },
                                        label = { Text(etiqueta) }
                                    )
                                }
                            }

                            Text(
                                "Ordenar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                listOf(
                                    "FECHA_DESC" to "Más reciente",
                                    "IMPORTE_DESC" to "Mayor importe",
                                    "IMPORTE_ASC" to "Menor importe"
                                ).forEach { (valor, etiqueta) ->
                                    FilterChip(
                                        selected = filtroOrden == valor,
                                        onClick = { filtroOrden = valor },
                                        label = { Text(etiqueta) }
                                    )
                                }
                            }

                            Text(
                                "Categoría",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(top = 12.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { desplegableCategoriaAbierto = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(textoFiltroCategoria)
                                }

                                DropdownMenu(
                                    expanded = desplegableCategoriaAbierto,
                                    onDismissRequest = { desplegableCategoriaAbierto = false }
                                ) {
                                    categoriasFiltro.forEach { (valor, etiqueta) ->
                                        DropdownMenuItem(
                                            text = { Text(etiqueta) },
                                            onClick = {
                                                filtroCategoria = valor
                                                desplegableCategoriaAbierto = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (gastos.isEmpty()) {
                item {
                    Text(
                        text = "No hay gastos todavía",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        if (kotlin.math.abs(deudaNeta) > 0.009) {
                            val mensajeDeuda = if (deudaNeta > 0) {
                                "Le debes ${"%.2f".format(deudaNeta)}€ a $nombreOtro"
                            } else {
                                "$nombreOtro te debe ${"%.2f".format(-deudaNeta)}€"
                            }

                            val textoBotonGlobal = if (deudaNeta > 0) {
                                "Has saldado tu deuda"
                            } else {
                                "Ha saldado su deuda"
                            }

                            val rolDeudorGlobal = if (deudaNeta > 0) rolActual else rolOtro

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = VerdeSuave),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = VerdePrincipal)
                                    ) {
                                        Text(
                                            text = mensajeDeuda,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val resultado = withContext(Dispatchers.IO) {
                                                    service.saldarDeudaGlobal(idPareja, rolDeudorGlobal)
                                                }
                                                if (resultado.isSuccess) {
                                                    recargarInterno++
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = VerdePrincipal,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(textoBotonGlobal)
                                    }
                                }
                            }
                        }

                        if (resumen.pendienteGeneralYo > 0.009 || resumen.pendienteGeneralOtro > 0.009) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = VerdeSuave),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "Pendientes generales",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text(
                                        text = "Tu parte pendiente: ${"%.2f".format(resumen.pendienteGeneralYo)}€",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "$nombreOtro: ${"%.2f".format(resumen.pendienteGeneralOtro)}€",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                gastosAgrupados.forEach { (fecha, gastosDelDia) ->
                    item(key = "fecha_$fecha") {
                        Text(
                            text = formatearCabeceraFecha(fecha),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(gastosDelDia, key = { it.idGasto }) { gasto ->
                        if (gasto.tipoGasto == "FIJO") {
                            GastoFijoCard(
                                gasto = gasto,
                                rolUsuario = rolActual,
                                onClick = { onOpenExpenseDetail(gasto, rolActual) }
                            )
                        } else {
                            GastoResumenCard(
                                gasto = gasto,
                                onClick = { onOpenExpenseDetail(gasto, rolActual) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GastoResumenCard(
    gasto: Gasto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdePrincipal
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = gasto.tituloGasto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${"%.2f".format(gasto.cantidadTotal)}€",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun GastoFijoCard(
    gasto: Gasto,
    rolUsuario: String,
    onClick: () -> Unit
) {
    val yoPague = if (rolUsuario == "USUARIO_1") gasto.pagadoUsuario1 else gasto.pagadoUsuario2
    val otroPago = if (rolUsuario == "USUARIO_1") gasto.pagadoUsuario2 else gasto.pagadoUsuario1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdePrincipal
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = gasto.tituloGasto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${"%.2f".format(gasto.cantidadTotal)}€",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Categoría: ${gasto.nombreCategoria}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (gasto.importeUsuario1 != null && gasto.importeUsuario2 != null) {
                Text(
                    text = "Reparto: ${"%.2f".format(gasto.importeUsuario1)}€ / ${"%.2f".format(gasto.importeUsuario2)}€",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (gasto.comentario.isNotBlank()) {
                Text(
                    text = gasto.comentario,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (gasto.esPendiente) {
                Text(
                    text = if (yoPague) "✅ Tú ya pagaste tu parte" else "⏳ Tú aún no has pagado",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = if (otroPago) "✅ Tu pareja ya pagó su parte" else "⏳ Tu pareja aún no ha pagado",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = "✅ Saldado",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}