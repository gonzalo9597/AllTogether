package com.example.alltogether.couples

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.alltogether.R
import com.example.alltogether.addcouple.AddCoupleActivity
import com.example.alltogether.dashboard.DashboardActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.selectoricono.SelectorIconoParejaActivity
import com.example.alltogether.settings.SettingsActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MisParejasActivity : ComponentActivity() {

    private val service = AllTogetherService()
    private lateinit var parejaPrefs: ParejaPreferencesManager
    private var parejasVisibles by mutableStateOf<List<ParejaVisual>>(emptyList())
    private var idParejaSeleccionandoIcono by mutableIntStateOf(-1)

    private val selectorIconoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && idParejaSeleccionandoIcono != -1) {
                val nuevoNombreIcono = result.data?.getStringExtra("icono_nombre")
                val nuevaRutaImagen = result.data?.getStringExtra("imagen_personalizada_path")

                if (nuevoNombreIcono != null) {
                    parejaPrefs.guardarIconoPareja(idParejaSeleccionandoIcono, nuevoNombreIcono)
                }

                if (nuevaRutaImagen != null) {
                    parejaPrefs.guardarImagenPersonalizadaPareja(
                        idParejaSeleccionandoIcono,
                        nuevaRutaImagen
                    )
                }

                cargarParejas()
                idParejaSeleccionandoIcono = -1
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
                    onOpenDashboard = ::abrirDashboard,
                    onOpenSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onOpenAddCouple = {
                        startActivity(Intent(this, AddCoupleActivity::class.java))
                    },
                    onOpenIconSelector = ::abrirSelectorIcono
                )
            }
        }

        cargarParejas()
    }

    override fun onResume() {
        super.onResume()
        cargarParejas()
    }

    private fun abrirDashboard(idPareja: Int, nombreParejaOriginal: String) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("id_pareja", idPareja)
        intent.putExtra("nombre_pareja", nombreParejaOriginal)
        startActivity(intent)
    }

    private fun abrirSelectorIcono(idPareja: Int) {
        idParejaSeleccionandoIcono = idPareja
        val intent = Intent(this, SelectorIconoParejaActivity::class.java)
        selectorIconoLauncher.launch(intent)
    }

    private fun cargarParejas() {
        lifecycleScope.launch {
            val parejasOriginales = withContext(Dispatchers.IO) {
                service.getParejasUsuario()
            }

            parejasVisibles = parejasOriginales.map { pareja ->
                ParejaVisual(
                    idPareja = pareja.idPareja,
                    nombreParejaOriginal = pareja.nombrePareja,
                    nombreVisible = parejaPrefs.obtenerNombreVisible(
                        pareja.idPareja,
                        pareja.nombrePareja
                    ),
                    iconoResId = parejaPrefs.obtenerIconoParejaResId(pareja.idPareja),
                    rutaImagenPersonalizada = parejaPrefs.obtenerImagenPersonalizadaPareja(
                        pareja.idPareja
                    )
                )
            }
        }
    }
}

data class ParejaVisual(
    val idPareja: Int,
    val nombreParejaOriginal: String,
    val nombreVisible: String,
    val iconoResId: Int,
    val rutaImagenPersonalizada: String? = null
)

private val FondoPantalla = Color.Black
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMisParejas(
    parejas: List<ParejaVisual>,
    onOpenDashboard: (Int, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddCouple: () -> Unit,
    onOpenIconSelector: (Int) -> Unit
) {
    val titulo = if (parejas.size == 1) "Mi pareja" else "Mis parejas"

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo AllTogether",
                        modifier = Modifier.size(width = 210.dp, height = 74.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdePrincipal
                ),
                actions = {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Ajustes",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            PieInicio()
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoPantalla)
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp, top = 34.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(34.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VerdeSuave
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
                    )
                }
            }

            items(parejas) { pareja ->
                ParejaCard(
                    nombrePareja = pareja.nombreVisible,
                    iconoResId = pareja.iconoResId,
                    rutaImagenPersonalizada = pareja.rutaImagenPersonalizada,
                    onClick = {
                        onOpenDashboard(
                            pareja.idPareja,
                            pareja.nombreParejaOriginal
                        )
                    },
                    onIconClick = {
                        onOpenIconSelector(pareja.idPareja)
                    }
                )
            }

            item {
                BotonAnadirPareja(
                    onClick = onOpenAddCouple
                )
            }
        }
    }
}

@Composable
fun ParejaCard(
    nombrePareja: String,
    iconoResId: Int,
    rutaImagenPersonalizada: String?,
    onClick: () -> Unit,
    onIconClick: () -> Unit
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
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .size(60.dp)
                    .clickable { onIconClick() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = VerdeSuave
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                if (rutaImagenPersonalizada != null) {
                    ImagenParejaGuardadaMini(
                        rutaImagen = rutaImagenPersonalizada,
                        fallbackResId = iconoResId,
                        contentDescription = nombrePareja,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = iconoResId),
                        contentDescription = nombrePareja,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = nombrePareja,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ImagenParejaGuardadaMini(
    rutaImagen: String?,
    fallbackResId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val bitmap = runCatching {
        if (rutaImagen.isNullOrBlank()) {
            null
        } else {
            BitmapFactory.decodeFile(File(rutaImagen).absolutePath)
        }
    }.getOrNull()

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = fallbackResId),
            contentDescription = contentDescription,
            modifier = modifier.padding(8.dp)
        )
    }
}

@Composable
fun BotonAnadirPareja(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(VerdeSuave),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Añadir pareja",
                tint = VerdePrincipal,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = VerdeSuave
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "Añadir pareja",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun PieInicio() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(top = 8.dp, bottom = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Proyecto Intermodular de DAM · AllTogether",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
        Text(
            text = "Sergio Malón, Sergio Sanz, Gonzalo Sebastián · v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
        Text(
            text = "2025-2026",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}