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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.alltogether.R
import com.example.alltogether.addexpense.AddExpenseActivity
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

    private val ajustesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cargarDatosPantalla()
                huboCambios = true

                // Si venimos de abandonar la pareja, cerramos el Dashboard también
                // MisParejasActivity recargará la lista al hacer onResume
                finish()
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
        startActivity(intent)
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
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val service = remember { AllTogetherService() }
    val coroutineScope = rememberCoroutineScope()

    // contador que usamos para forzar la recarga de gastos
    var recargar by remember { mutableIntStateOf(0) }
    var gastos by remember { mutableStateOf<List<Gasto>>(emptyList()) }

    LaunchedEffect(recargar) {
        val nuevosGastos = withContext(Dispatchers.IO) {
            service.getGastos(idPareja)
        }
        android.util.Log.d("GASTOS", "Cargados ${nuevosGastos.size} gastos, recargar=$recargar")
        nuevosGastos.forEach {
            android.util.Log.d("GASTOS", "Gasto ${it.idGasto}: esPendiente=${it.esPendiente}")
        }
        gastos = nuevosGastos
    }

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
                Button(
                    onClick = onAddExpenseClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Añadir gasto")
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
                    val totalPendiente = gastos
                        .filter { it.esPendiente }
                        .sumOf { it.cantidadTotal }
                    Text(
                        text = "Total pendiente: ${"%.2f".format(totalPendiente)}€",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(gastos) { gasto ->
                    GastoCard(
                        gasto = gasto,
                        onSaldar = { idGasto ->
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    service.saldarDeuda(idGasto)
                                }
                                // Incrementar el contador para forzar recarga
                                recargar++
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GastoCard(gasto: Gasto, onSaldar: (Int) -> Unit) {
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

            Text(
                text = if (gasto.esPendiente) "⏳ Pendiente" else "✅ Saldado",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (gasto.esPendiente) {
                Button(
                    onClick = { onSaldar(gasto.idGasto) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Marcar mi parte como pagada")
                }
            }
        }
    }
}