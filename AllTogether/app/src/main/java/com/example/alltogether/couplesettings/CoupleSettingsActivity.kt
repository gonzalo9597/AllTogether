package com.example.alltogether.couplesettings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.selectoricono.SelectorIconoParejaActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.ParejaPreferencesManager

class CoupleSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val idPareja = intent.getIntExtra("id_pareja", -1)
        val nombrePareja = intent.getStringExtra("nombre_pareja") ?: "Pareja"

        setContent {
            AllTogetherTheme {
                PantallaCoupleSettings(idPareja, nombrePareja)
            }
        }
    }
}

@Composable
fun PantallaCoupleSettings(idPareja: Int, nombrePareja: String) {
    val context = LocalContext.current
    val prefs = remember { ParejaPreferencesManager(context) }

    var nombreVisible by remember {
        mutableStateOf(prefs.obtenerNombreVisible(idPareja, nombrePareja))
    }

    var nombreIcono by remember {
        mutableStateOf(prefs.obtenerNombreIconoPareja(idPareja))
    }

    val selectorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val nuevoNombreIcono =
                result.data?.getStringExtra("icono_nombre") ?: nombreIcono
            nombreIcono = nuevoNombreIcono
        }
    }

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Ajustes pareja",
                onBackClick = { (context as ComponentActivity).finish() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Icono de la pareja")

            Image(
                painter = painterResource(id = prefs.nombreIconoAResId(nombreIcono)),
                contentDescription = "Icono pareja",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 12.dp)
                    .clickable {
                        val intent = Intent(context, SelectorIconoParejaActivity::class.java)
                        selectorLauncher.launch(intent)
                    }
            )

            OutlinedTextField(
                value = nombreVisible,
                onValueChange = { nombreVisible = it },
                label = { Text("Nombre pareja") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            )

            Button(
                onClick = {
                    prefs.guardarNombreVisible(idPareja, nombreVisible)
                    prefs.guardarIconoPareja(idPareja, nombreIcono)

                    val activity = context as ComponentActivity
                    activity.setResult(Activity.RESULT_OK)
                    activity.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Guardar")
            }
        }
    }
}