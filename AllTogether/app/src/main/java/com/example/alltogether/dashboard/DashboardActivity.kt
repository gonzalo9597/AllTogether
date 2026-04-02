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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
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
    private val service = AllTogetherService()

    private var nombreVisible by mutableStateOf("Pareja")
    private var huboCambios by mutableStateOf(false)
    // Lista de gastos que se muestra en pantalla
    private var gastos by mutableStateOf<List<Gasto>>(emptyList())

    private val ajustesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cargarDatosPantalla()
                huboCambios = true
            }
        }

    private val addExpenseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
                // Recargar gastos al volver de añadir gasto
                cargarGastos()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        idPareja = intent.getIntExtra("id_pareja", -1)
        nombreParejaOriginal = intent.getStringExtra("nombre_pareja") ?: "Pareja"
        parejaPrefs = ParejaPreferencesManager(this)

        cargarDatosPantalla()
        cargarGastos()

        setContent {
            AllTogetherTheme {
                PantallaDashboard(
                    nombrePareja = nombreVisible,
                    idPareja = idPareja,
                    gastos = gastos,
                    onBackClick = { finish() },
                    onSettingsClick = { abrirAjustesPareja() },
                    onAddExpenseClick = { abrirAnadirGasto() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarGastos()
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

    // Carga los gastos de la pareja desde la API en segundo plano
    private fun cargarGastos() {
        lifecycleScope.launch {
            gastos = withContext(Dispatchers.IO) {
                service.getGastos(idPareja)
            }
        }
    }

    override fun finish() {
        if (huboCambios) setResult(Activity.RESULT_OK)
        super.finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun PantallaDashboard(
    nombrePareja: String,
    idPareja: Int,
    gastos: List<Gasto>,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val context = LocalContext.current

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
                // Resumen de gastos pendientes
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

                // Lista de gastos
                items(gastos) { gasto ->
                    GastoCard(gasto = gasto)
                }
            }
        }
    }
}

// Tarjeta que muestra los datos de un gasto
@androidx.compose.runtime.Composable
fun GastoCard(gasto: Gasto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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

            // Mostrar el reparto
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
        }
    }
}