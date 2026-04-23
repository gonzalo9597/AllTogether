package com.example.alltogether.selectoricono

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.R
import com.example.alltogether.ui.theme.AllTogetherTheme
import java.io.File
import java.io.FileOutputStream

private val FondoPantalla = Color(0xFF383A39)
private val VerdePrincipal = Color(0xFF2DBC94)
private val VerdeSuave = Color(0xFFA6E6DB)

class SelectorIconoParejaActivity : ComponentActivity() {

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val rutaGuardada = copiarImagenAAlmacenamientoInterno(uri)

                if (rutaGuardada != null) {
                    val intent = Intent()
                    intent.putExtra("imagen_personalizada_path", rutaGuardada)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AllTogetherTheme {
                PantallaSelectorIconoPareja(
                    onBackClick = { finish() },
                    onElegirFoto = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onIconoSeleccionado = { nombreIcono ->
                        val intent = Intent()
                        intent.putExtra("icono_nombre", nombreIcono)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                )
            }
        }
    }

    private fun copiarImagenAAlmacenamientoInterno(uri: Uri): String? {
        return try {
            val carpeta = File(filesDir, "imagenes_pareja")
            if (!carpeta.exists()) {
                carpeta.mkdirs()
            }

            val archivoDestino = File(
                carpeta,
                "pareja_${System.currentTimeMillis()}.jpg"
            )

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(archivoDestino).use { output ->
                    input.copyTo(output)
                }
            }

            archivoDestino.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSelectorIconoPareja(
    onBackClick: () -> Unit,
    onElegirFoto: () -> Unit,
    onIconoSeleccionado: (String) -> Unit
) {
    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Elegir icono",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = Color.White
                    )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selecciona un icono",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            TarjetaFotoGaleria(
                onClick = onElegirFoto
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconoParejaItem(
                    iconoResId = R.drawable.corazon,
                    nombreIcono = "corazon",
                    onIconoSeleccionado = onIconoSeleccionado,
                    modifier = Modifier.weight(1f)
                )
                IconoParejaItem(
                    iconoResId = R.drawable.casa,
                    nombreIcono = "casa",
                    onIconoSeleccionado = onIconoSeleccionado,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconoParejaItem(
                    iconoResId = R.drawable.pareja,
                    nombreIcono = "pareja",
                    onIconoSeleccionado = onIconoSeleccionado,
                    modifier = Modifier.weight(1f)
                )
                IconoParejaItem(
                    iconoResId = R.drawable.viaje,
                    nombreIcono = "viaje",
                    onIconoSeleccionado = onIconoSeleccionado,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconoParejaItem(
                    iconoResId = R.drawable.cafe,
                    nombreIcono = "cafe",
                    onIconoSeleccionado = onIconoSeleccionado,
                    modifier = Modifier.weight(1f)
                )
                IconoParejaItem(
                    iconoResId = R.drawable.gato,
                    nombreIcono = "gato",
                    onIconoSeleccionado = onIconoSeleccionado,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TarjetaFotoGaleria(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdeSuave
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Photo,
                    contentDescription = "Elegir foto",
                    tint = VerdePrincipal,
                    modifier = Modifier.size(52.dp)
                )

                Text(
                    text = "Elegir foto del móvil",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun IconoParejaItem(
    iconoResId: Int,
    nombreIcono: String,
    onIconoSeleccionado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onIconoSeleccionado(nombreIcono) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdeSuave
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconoResId),
                contentDescription = nombreIcono,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}