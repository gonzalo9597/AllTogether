
package com.example.alltogether.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Objeto singleton — solo existe una instancia en toda la app
// Centraliza toda la configuración HTTP para no repetirla en cada petición
object ApiClient {

    // URL base de nuestra API en AWS — todas las peticiones parten de aquí
    const val BASE_URL = "https://hmslzcrv00.execute-api.eu-south-2.amazonaws.com"

    // El JWT token del usuario autenticado
    // Es null cuando no hay sesión iniciada
    // Se rellena tras el login y se incluye automáticamente en cada petición
    var token: String? = null

    // Cliente HTTP compartido por toda la app
    // CIO es el motor recomendado para Android con Ktor
    val http = HttpClient(CIO) {

        // Plugin que convierte automáticamente entre JSON y objetos Kotlin
        // ignoreUnknownKeys = true evita crashes si el servidor añade campos nuevos
        // que aún no tenemos en nuestros data classes
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        // Esta configuración se aplica a TODAS las peticiones automáticamente
        // Si hay token, añade el header "Authorization: Bearer <token>"
        // Así no hay que acordarse de añadirlo manualmente en cada llamada
        defaultRequest {
            token?.let { header("Authorization", "Bearer $it") }
        }
    }
}