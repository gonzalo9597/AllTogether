package com.example.alltogether.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.res.painterResource
import com.example.alltogether.R
import com.example.alltogether.addexpense.AddExpenseActivity
import com.example.alltogether.addexpense.AddRecurringExpenseActivity
import com.example.alltogether.couplesettings.CoupleSettingsActivity
import com.example.alltogether.model.Gasto
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : ComponentActivity() {

    private var idPareja: Int = -1
    private var nombreParejaOriginal: String = "Pareja"
    private lateinit var parejaPrefs: ParejaPreferencesManager
    private var huboCambios by mutableStateOf(false)
    private var nombreVisible by mutableStateOf("Pareja")
    // Contador para forzar recarga de gastos desde la Activity
    private var recargarGastos by mutableStateOf(0)

    private val ajustesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cargarDatosPantalla()
                huboCambios = true
                finish()
            }
        }

    // Launcher para añadir gasto — recarga los gastos al volver
    private val addExpenseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            recargarGastos++
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
                    onAddExpenseClick = { abrirAnadirGasto() }
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

    private fun cargarDatosPantalla() {
        nombreVisible = parejaPrefs.obtenerNombreVisible(idPareja, nombreParejaOriginal)
    }

    override fun finish() {
        if (huboCambios) setResult(Activity.RESULT_OK)
        super.finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDashboard(
    nombrePareja: String,
    idPareja: Int,
    recargar: Int,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()
    var gastos by remember { mutableStateOf<List<Gasto>>(emptyList()) }
    var gastoEditando by remember { mutableStateOf<Gasto?>(null) }
    var recargarInterno by remember { mutableStateOf(recargar) }
    var balance by remember { mutableStateOf<AllTogetherService.Balance?>(null) }
    var filtroEstado by remember { mutableStateOf("TODOS") }
    var filtroFecha by remember { mutableStateOf("TODOS") }
    var filtroOrden by remember { mutableStateOf("FECHA_DESC") }
    var filtroCategoria by remember { mutableStateOf("TODAS") }
    var mostrarFiltros by remember { mutableStateOf(false) }

    LaunchedEffect(recargar, recargarInterno) {
        gastos = withContext(Dispatchers.IO) { service.getGastos(idPareja) }
        val balanceResult = withContext(Dispatchers.IO) { service.getBalance(idPareja) }
        balanceResult.onSuccess { balance = it }
    }

    // Diálogo de edición
    gastoEditando?.let { gasto ->
        EditarGastoDialog(
            gasto = gasto,
            onConfirmar = { titulo, cantidad, comentario ->
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        service.editarGasto(
                            idGasto = gasto.idGasto,
                            tituloGasto = titulo,
                            cantidadTotal = cantidad,
                            comentario = comentario
                        )
                    }
                    gastoEditando = null
                    recargarInterno++
                }
            },
            onCancelar = { gastoEditando = null }
        )
    }

    // Filtros aplicados antes del Scaffold
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
                "IMPORTE_DESC" -> compareByDescending { it.cantidadTotal }
                "IMPORTE_ASC" -> compareBy { it.cantidadTotal }
                else -> compareByDescending { it.fechaGasto }
            }
        )

    val categorias = gastos.map { it.nombreCategoria }.distinct().sorted()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = nombrePareja) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Text("←") }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Image(
                            painter = painterResource(id = R.drawable.ajustes),
                            contentDescription = "Ajustes pareja",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val context = LocalContext.current
                Button(onClick = onAddExpenseClick, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Añadir gasto")
                }
                Button(
                    onClick = {
                        val intent = Intent(context, AddRecurringExpenseActivity::class.java)
                        intent.putExtra("id_pareja", idPareja)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Gasto recurrente")
                }
            }

            item {
                Button(
                    onClick = { mostrarFiltros = !mostrarFiltros },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (mostrarFiltros) "Ocultar filtros" else "🔍 Filtros")
                }

                if (mostrarFiltros) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Estado:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("TODOS" to "Todos", "PENDIENTE" to "Pendiente", "SALDADO" to "Saldado").forEach { (valor, etiqueta) ->
                                FilterChip(selected = filtroEstado == valor, onClick = { filtroEstado = valor }, label = { Text(etiqueta) })
                            }
                        }

                        Text("Fecha:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("TODOS" to "Todos", "ESTE_MES" to "Este mes", "MES_ANTERIOR" to "Mes anterior").forEach { (valor, etiqueta) ->
                                FilterChip(selected = filtroFecha == valor, onClick = { filtroFecha = valor }, label = { Text(etiqueta) })
                            }
                        }

                        Text("Ordenar:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("FECHA_DESC" to "Más reciente", "IMPORTE_DESC" to "Mayor importe", "IMPORTE_ASC" to "Menor importe").forEach { (valor, etiqueta) ->
                                FilterChip(selected = filtroOrden == valor, onClick = { filtroOrden = valor }, label = { Text(etiqueta) })
                            }
                        }

                        if (categorias.isNotEmpty()) {
                            Text("Categoría:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = filtroCategoria == "TODAS", onClick = { filtroCategoria = "TODAS" }, label = { Text("Todas") })
                                categorias.forEach { categoria ->
                                    FilterChip(selected = filtroCategoria == categoria, onClick = { filtroCategoria = categoria }, label = { Text(categoria) })
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
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else {
                item {
                    val rolActual = balance?.rolUsuario ?: "USUARIO_1"
                    val totalPendiente = gastos
                        .filter { it.esPendiente }
                        .sumOf { gasto ->
                            val yoPague = if (rolActual == "USUARIO_1") gasto.pagadoUsuario1 else gasto.pagadoUsuario2
                            if (!yoPague) {
                                if (rolActual == "USUARIO_1") gasto.importeUsuario1 ?: 0.0
                                else gasto.importeUsuario2 ?: 0.0
                            } else 0.0
                        }
                    Text(
                        text = "Tu parte pendiente: ${"%.2f".format(totalPendiente)}€",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    balance?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    it.diferencia > 0 -> MaterialTheme.colorScheme.errorContainer
                                    it.diferencia < 0 -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                }
                            )
                        ) {
                            Text(text = it.mensaje, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                items(gastosFiltrados) { gasto ->
                    GastoCard(
                        gasto = gasto,
                        rolUsuario = balance?.rolUsuario ?: "USUARIO_1",
                        onSaldar = { idGasto ->
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) { service.saldarDeuda(idGasto) }
                                recargarInterno++
                            }
                        },
                        onEditar = { gastoAEditar -> gastoEditando = gastoAEditar },
                        onEliminar = { idGasto ->
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) { service.eliminarGasto(idGasto) }
                                recargarInterno++
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun GastoCard(
    gasto: Gasto,
    rolUsuario: String,
    onSaldar: (Int) -> Unit,
    onEditar: (Gasto) -> Unit,
    onEliminar: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (gasto.esPendiente)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${"%.2f".format(gasto.cantidadTotal)}€",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = gasto.fechaGasto,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (gasto.importeUsuario1 != null && gasto.importeUsuario2 != null) {
                Text(
                    text = "Reparto: ${"%.2f".format(gasto.importeUsuario1)}€ / ${"%.2f".format(gasto.importeUsuario2)}€",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (gasto.comentario.isNotBlank()) {
                Text(
                    text = gasto.comentario,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

// Determinar si yo ya pagué y si el otro ya pagó
            val yoPague = if (rolUsuario == "USUARIO_1") gasto.pagadoUsuario1 else gasto.pagadoUsuario2
            val otroPago = if (rolUsuario == "USUARIO_1") gasto.pagadoUsuario2 else gasto.pagadoUsuario1

// Mostrar estado de pago de cada usuario
            if (gasto.esPendiente) {
                Text(
                    text = if (yoPague) "✅ Tú ya pagaste tu parte" else "⏳ Tú aún no has pagado",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = if (otroPago) "✅ Tu pareja ya pagó su parte" else "⏳ Tu pareja aún no ha pagado",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = "✅ Saldado",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

// Solo mostrar el botón de saldar si el gasto sigue pendiente Y yo no he pagado aún
            if (gasto.esPendiente && !yoPague) {
                Button(
                    onClick = { onSaldar(gasto.idGasto) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Marcar mi parte como pagada")
                }
            }

            // Botones de editar y eliminar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onEditar(gasto) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }
                Button(
                    onClick = { onEliminar(gasto.idGasto) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Eliminar")
                }
            }
        }
    }

}
@Composable
fun EditarGastoDialog(
    gasto: Gasto,
    onConfirmar: (String, Double, String) -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf(gasto.tituloGasto) }
    var cantidad by remember { mutableStateOf(gasto.cantidadTotal.toString()) }
    var comentario by remember { mutableStateOf(gasto.comentario) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad (€)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val cantidadDouble = cantidad.toDoubleOrNull()
                if (titulo.isNotBlank() && cantidadDouble != null && cantidadDouble > 0) {
                    onConfirmar(titulo, cantidadDouble, comentario)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

