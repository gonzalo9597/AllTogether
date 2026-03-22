package com.example.alltogether.couples

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.alltogether.R
import com.example.alltogether.addcouple.AddCoupleActivity
import com.example.alltogether.dashboard.DashboardActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.settings.SettingsActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MisParejasActivity : ComponentActivity() {

    // Servicio mock de parejas
    private val service = AllTogetherService()

    // Preferencias para guardar nombre e icono visibles
    private lateinit var parejaPrefs: ParejaPreferencesManager

    // Lista que realmente pinta Compose en pantalla
    private var parejasVisibles by mutableStateOf<List<ParejaVisual>>(emptyList())

    private val dashboardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Si desde dashboard ha habido cambios, recargamos
            if (result.resultCode == Activity.RESULT_OK) {
                cargarParejas()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        parejaPrefs = ParejaPreferencesManager(this)

        setContent {
            AllTogetherTheme {
                PantallaMisParejas(
                    parejas = parejasVisibles,
                    onOpenDashboard = { idPareja, nombreParejaOriginal ->
                        abrirDashboard(idPareja, nombreParejaOriginal)
                    },
                    onOpenSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onOpenAddCouple = {
                        startActivity(Intent(this, AddCoupleActivity::class.java))
                    }
                )
            }
        }

        cargarParejas()
    }

    override fun onResume() {
        super.onResume()
        // Cada vez que vuelvas a esta pantalla, recargo
        cargarParejas()
    }

    private fun abrirDashboard(idPareja: Int, nombreParejaOriginal: String) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("id_pareja", idPareja)
        intent.putExtra("nombre_pareja", nombreParejaOriginal)
        dashboardLauncher.launch(intent)
    }

    private fun cargarParejas() {
        lifecycleScope.launch {
            val parejasOriginales = withContext(Dispatchers.IO) {
                service.getParejasUsuario(1)
            }

            // Aquí ya monto la lista con nombre visible e icono visible
            parejasVisibles = parejasOriginales.map { pareja ->
                ParejaVisual(
                    idPareja = pareja.idPareja,
                    nombreParejaOriginal = pareja.nombrePareja,
                    nombreVisible = parejaPrefs.obtenerNombreVisible(
                        pareja.idPareja,
                        pareja.nombrePareja
                    ),
                    iconoResId = parejaPrefs.obtenerIconoParejaResId(
                        pareja.idPareja
                    )
                )
            }
        }
    }
}

// Modelo simple solo para pintar la pantalla
data class ParejaVisual(
    val idPareja: Int,
    val nombreParejaOriginal: String,
    val nombreVisible: String,
    val iconoResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMisParejas(
    parejas: List<ParejaVisual>,
    onOpenDashboard: (Int, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddCouple: () -> Unit
) {
    val tituloParejas = if (parejas.size == 1) "Mi pareja" else "Mis parejas"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "AllTogether",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = onOpenSettings
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ajustes),
                            contentDescription = "Ajustes",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = tituloParejas,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            items(parejas) { pareja ->
                ParejaCard(
                    nombrePareja = pareja.nombreVisible,
                    iconoResId = pareja.iconoResId,
                    onClick = {
                        onOpenDashboard(
                            pareja.idPareja,
                            pareja.nombreParejaOriginal
                        )
                    }
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAddCouple() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "+ Añadir pareja", fontSize = 22.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ParejaCard(
    nombrePareja: String,
    iconoResId: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconoResId),
                contentDescription = nombrePareja,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = nombrePareja, fontSize = 22.sp)
            }
        }
    }
}