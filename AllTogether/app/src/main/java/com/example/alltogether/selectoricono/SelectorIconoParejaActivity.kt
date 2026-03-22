package com.example.alltogether.selectoricono

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alltogether.R
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.ui.theme.AllTogetherTheme

class SelectorIconoParejaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AllTogetherTheme {
                PantallaSelectorIconoPareja(
                    onBackClick = { finish() },
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
}

@Composable
fun PantallaSelectorIconoPareja(
    onBackClick: () -> Unit,
    onIconoSeleccionado: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Elegir icono",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selecciona un icono",
                fontSize = 22.sp
            )

            Row(modifier = Modifier.fillMaxWidth()) {
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

            Row(modifier = Modifier.fillMaxWidth()) {
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

            Row(modifier = Modifier.fillMaxWidth()) {
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
fun IconoParejaItem(
    iconoResId: Int,
    nombreIcono: String,
    onIconoSeleccionado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clickable { onIconoSeleccionado(nombreIcono) }
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