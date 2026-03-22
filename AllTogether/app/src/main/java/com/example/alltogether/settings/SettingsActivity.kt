package com.example.alltogether.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.alltogether.TopAppBarWithBack
import com.example.alltogether.login.LoginActivity
import com.example.alltogether.ui.theme.AllTogetherTheme
import com.example.alltogether.util.SessionManager

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AllTogetherTheme {
                PantallaSettings()
            }
        }
    }
}

@Composable
fun PantallaSettings() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var divisa by remember { mutableStateOf("EUR") }

    Scaffold(
        topBar = {
            TopAppBarWithBack(
                title = "Configuración",
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
            OutlinedTextField(
                value = divisa,
                onValueChange = { divisa = it },
                label = { Text("Divisa preferida") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { (context as ComponentActivity).finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Guardar")
            }

            Button(
                onClick = {
                    sessionManager.clearSession()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as ComponentActivity).finishAffinity()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
