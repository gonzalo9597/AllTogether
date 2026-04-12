package com.example.alltogether.couplesettings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.model.GastoRecurrente
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecurringExpensesActivity : ComponentActivity() {

    private var idPareja: Int = -1
    private var recargar by mutableIntStateOf(0)

    private val editarLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                recargar++
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        idPareja = intent.getIntExtra("id_pareja", -1)

        setContent {
            AllTogetherTheme {
                PantallaRecurringExpenses(
                    idPareja = idPareja,
                    recargar = recargar,
                    onBackClick = { finish() },
                    onOpenEdit = { recurrente ->
                        val intent = Intent(this, EditRecurringExpenseActivity::class.java)
                        intent.putExtra("recurrente", recurrente)
                        editarLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaRecurringExpenses(
    idPareja: Int,
    recargar: Int,
    onBackClick: () -> Unit,
    onOpenEdit: (GastoRecurrente) -> Unit
) {
    val service = remember { AllTogetherService() }
    var recurrentes by remember { mutableStateOf<List<GastoRecurrente>>(emptyList()) }

    LaunchedEffect(idPareja, recargar) {
        recurrentes = withContext(Dispatchers.IO) {
            service.getRecurrentes(idPareja)
        }
    }

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Gastos recurrentes",
                onBackClick = onBackClick
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
            if (recurrentes.isEmpty()) {
                item {
                    Text(
                        text = "No hay gastos recurrentes creados",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                items(recurrentes) { recurrente ->
                    RecurrenteCard(
                        recurrente = recurrente,
                        onClick = { onOpenEdit(recurrente) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecurrenteCard(
    recurrente: GastoRecurrente,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = recurrente.tituloGasto,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatearFrecuencia(recurrente.frecuencia),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun formatearFrecuencia(frecuencia: String): String {
    return when (frecuencia) {
        "DIARIO" -> "Diario"
        "SEMANAL" -> "Semanal"
        "MENSUAL" -> "Mensual"
        "ANUAL" -> "Anual"
        else -> frecuencia
    }
}