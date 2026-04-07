
package com.example.alltogether.network

import android.content.Context
import android.content.Intent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {

    const val BASE_URL = "https://hmslzcrv00.execute-api.eu-south-2.amazonaws.com"

    var token: String? = null

    // Contexto de la app necesario para redirigir a Login cuando el token caduca
    var appContext: Context? = null

    val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 30000
        }
        defaultRequest {
            token?.let { header("Authorization", "Bearer $it") }
        }

        // Interceptor que detecta respuestas 401 y cierra la sesión automáticamente
        install(HttpCallValidator) {
            validateResponse { response ->
                if (response.status.value == 401) {
                    // Token caducado o inválido — limpiar sesión y redirigir a Login
                    appContext?.let { ctx ->
                        token = null
                        val sessionManager = com.example.alltogether.util.SessionManager(ctx)
                        sessionManager.clearSession()

                        val intent = com.example.alltogether.login.LoginActivity::class.java
                        ctx.startActivity(
                            Intent(ctx, intent).apply {
                                // Limpiar el back stack para que no pueda volver atrás
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                putExtra("sesion_caducada", true)
                            }
                        )
                    }
                }
            }
        }
    }
}