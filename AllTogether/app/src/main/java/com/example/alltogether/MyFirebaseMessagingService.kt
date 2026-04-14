package com.example.alltogether

import android.os.Build
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

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "AllTogether"
        val body = message.notification?.body ?: message.data["body"] ?: ""

        val channelId = "alltogether_channel"
        val notificationManager = getSystemService(android.app.NotificationManager::class.java)

        // Crear el canal solo en Android 8+ (sin @RequiresApi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "AllTogether",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.corazon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}