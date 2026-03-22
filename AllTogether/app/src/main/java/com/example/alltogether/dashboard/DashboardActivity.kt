package com.example.alltogether.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.alltogether.R
import com.example.alltogether.addexpense.AddExpenseActivity
import com.example.alltogether.couplesettings.CoupleSettingsActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager

class DashboardActivity : ComponentActivity() {

    private var idPareja: Int = -1
    private var nombreParejaOriginal: String = "Pareja"

    private lateinit var parejaPrefs: ParejaPreferencesManager

    // Nombre que se muestra en pantalla
    private var nombreVisible by mutableStateOf("Pareja")

    // Para saber si al volver a MisParejas hay que refrescar
    private var huboCambios by mutableStateOf(false)

    private val ajustesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cargarDatosPantalla()
                huboCambios = true
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
                    onSettingsClick = { abrirAjustesPareja() }
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

    private fun cargarDatosPantalla() {
        nombreVisible = parejaPrefs.obtenerNombreVisible(idPareja, nombreParejaOriginal)
    }

    override fun finish() {
        if (huboCambios) {
            setResult(Activity.RESULT_OK)
        }
        super.finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun PantallaDashboard(
    nombrePareja: String,
    idPareja: Int,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = nombrePareja)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Text("←")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSettingsClick
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ajustes),
                            contentDescription = "Ajustes pareja",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resumen de la pareja",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Aquí irá el resumen de gastos, gastos fijos, espontáneos y saldo"
            )

            Button(
                onClick = {
                    val intent = Intent(context, AddExpenseActivity::class.java)
                    intent.putExtra("id_pareja", idPareja)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir gasto")
            }
        }
    }
}