package com.example.alltogether

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.alltogether.couples.MisParejasActivity
import com.example.alltogether.login.LoginActivity
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.network.ApiClient
import com.example.alltogether.util.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Punto de entrada de la app — decide a dónde ir al arrancar
// El usuario nunca ve esta pantalla, es solo lógica de navegación
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
// Pedir permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        if (sessionManager.isLoggedIn()) {
            // Restaurar token en el cliente HTTP
            ApiClient.token = sessionManager.getToken()
            ApiClient.appContext = applicationContext

            // Obtener y enviar el token FCM al servidor
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fcmToken = task.result
                    android.util.Log.d("FCM_TOKEN", "Token: $fcmToken")
                    CoroutineScope(Dispatchers.IO).launch {
                        AllTogetherService().guardarTokenFCM(fcmToken)
                    }
                } else {
                    android.util.Log.e("FCM_TOKEN", "Error obteniendo token: ${task.exception}")
                }
            }

            startActivity(Intent(this, MisParejasActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}