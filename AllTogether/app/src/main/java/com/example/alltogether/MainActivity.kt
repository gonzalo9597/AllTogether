package com.example.alltogether

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.alltogether.couples.MisParejasActivity
import com.example.alltogether.login.LoginActivity
import com.example.alltogether.network.ApiClient
import com.example.alltogether.util.SessionManager

// Punto de entrada de la app — decide a dónde ir al arrancar
// El usuario nunca ve esta pantalla, es solo lógica de navegación
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            // Hay sesión guardada — restaurar el token en el cliente HTTP
            // para que las peticiones lleven el Authorization header automáticamente
            ApiClient.token = sessionManager.getToken()

            // Ir directamente a MisParejas sin pasar por Login
            startActivity(Intent(this, MisParejasActivity::class.java))
        } else {
            // No hay sesión — ir a Login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Cerramos MainActivity para que el usuario no pueda volver atrás a ella
        finish()
    }
}