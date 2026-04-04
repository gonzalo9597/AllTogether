package com.example.alltogether

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.alltogether.network.AllTogetherService
import com.example.alltogether.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Se llama cuando Firebase genera un nuevo token para este dispositivo
    // Ocurre al instalar la app, reinstalarla o limpiar datos
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sessionManager = SessionManager(this)

        // Solo enviar el token si hay sesión activa
        if (sessionManager.isLoggedIn()) {
            CoroutineScope(Dispatchers.IO).launch {
                AllTogetherService().guardarTokenFCM(token)
            }
        }
    }

    // Se llama cuando llega una notificación mientras la app está en primer plano
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Por ahora solo logueamos — más adelante podemos mostrar una notificación
        android.util.Log.d("FCM", "Notificación recibida: ${message.notification?.title}")
    }
}