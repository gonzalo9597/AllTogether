package com.example.alltogether.couplesettings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.model.GastoRecurrente
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.CurrencyPreferencesManager
import com.example.alltogether.util.ScreenUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val FondoPantalla = Color(0xFF383A39)
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)
private val GrisTexto = Color(0xFF1F1F1F)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecurringExpenses(
    idPareja: Int,
    recargar: Int,
    onBackClick: () -> Unit,
    onOpenEdit: (GastoRecurrente) -> Unit
) {
    val context = LocalContext.current
    val service = remember { AllTogetherService() }
    val currencyManager = remember { CurrencyPreferencesManager(context) }
    val simboloDivisa = currencyManager.obtenerSimbolo()

    var recurrentes by remember { mutableStateOf<List<GastoRecurrente>>(emptyList()) }
    var mostrarCardInfo by remember { mutableStateOf(ScreenUiState.mostrarCardInfoRecurringExpenses) }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            mostrarCardInfo = false
            ScreenUiState.mostrarCardInfoRecurringExpenses = false
        }
    }

    LaunchedEffect(idPareja, recargar) {
        recurrentes = withContext(Dispatchers.IO) {
            service.getRecurrentes(idPareja)
        }
    }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = "Gastos recurrentes",
                            tint = Color.White
                        )

                        Spacer(modifier = Modifier.size(6.dp))

                        Text(
                            text = "Gastos recurrentes",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoPantalla)
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                AnimatedVisibility(visible = mostrarCardInfo) {
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = true,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {},
                        content = {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = VerdeSuave),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 18.dp)
                                ) {
                                    Text(
                                        text = "Tus gastos fijos",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GrisTexto
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Consulta y edita los gastos recurrentes de la pareja desde un solo sitio",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GrisTexto
                                    )
                                }
                            }
                        }
                    )
                }
            }

            if (recurrentes.isEmpty()) {
                item {
                    Text(
                        text = "No hay gastos recurrentes creados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            } else {
                items(recurrentes) { recurrente ->
                    RecurrenteCard(
                        recurrente = recurrente,
                        simboloDivisa = simboloDivisa,
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
    simboloDivisa: String,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = recurrente.tituloGasto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            val importeTexto = obtenerImporteRecurrente(recurrente)

            if (importeTexto != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "$importeTexto$simboloDivisa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VerdeSuave
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = formatearFrecuencia(recurrente.frecuencia),
                        color = Color.Black,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

fun obtenerImporteRecurrente(recurrente: GastoRecurrente): String? {
    return try {
        val campo = recurrente.javaClass.declaredFields.firstOrNull {
            it.name == "cantidadTotal" || it.name == "importe" || it.name == "cantidad"
        } ?: return null

        campo.isAccessible = true
        val valor = campo.get(recurrente) ?: return null

        when (valor) {
            is Double -> "%.2f".format(valor)
            is Float -> "%.2f".format(valor)
            is Int -> valor.toString()
            is Long -> valor.toString()
            else -> valor.toString()
        }
    } catch (e: Exception) {
        null
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