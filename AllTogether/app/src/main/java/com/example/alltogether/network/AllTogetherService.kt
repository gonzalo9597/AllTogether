package com.example.alltogether.network

import com.example.alltogether.model.Pareja
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import com.example.alltogether.model.GastoResponse
import com.example.alltogether.model.Gasto
// Estructura de la respuesta que devuelve el servidor tras login/register
// @Serializable permite que Ktor convierta automáticamente el JSON a este objeto
@Serializable
data class LoginResponse(
    val token: String,      // El JWT que usaremos como "DNI" en futuras peticiones
    val idUsuario: Int,     // ID del usuario en la base de datos
    val nombre: String,     // Nombre del usuario
    val email: String       // Email del usuario
)

// Lo que enviamos al servidor para hacer login
@Serializable
data class LoginRequest(val email: String, val password: String)

// Lo que enviamos al servidor para registrarse
@Serializable
data class RegisterRequest(val nombre: String, val email: String, val password: String)
// Datos que enviamos al servidor para guardar un gasto
@Serializable
data class GuardarGastoRequest(
    val idPareja: Int,
    val tituloGasto: String,
    val cantidadTotal: Double,
    val modoReparto: String = "MITAD",
    val comentario: String = "",
    val idCategoria: Int = 1,
    val porcentajeUsuario1: Double? = null,
    val porcentajeUsuario2: Double? = null,
    val importeUsuario1: Double? = null,
    val importeUsuario2: Double? = null
)
class AllTogetherService {

    // Intenta hacer login con email y contraseña
    // Devuelve Result.success con los datos si va bien, Result.failure si algo falla
    // Usando Result evitamos crashes — el error se maneja en la UI, no aquí
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            // Llamada POST a /login con el email y password en el body como JSON
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            if (response.status.value in 200..299) {
                // El servidor respondió OK — convertimos el JSON a LoginResponse
                Result.success(response.body<LoginResponse>())
            } else {
                // El servidor respondió con error (ej: 401 credenciales incorrectas)
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            // Error de red (sin internet, timeout, etc.)
            Result.failure(e)
        }
    }

    // Igual que login pero para crear una cuenta nueva
    // Si el registro va bien, el servidor también devuelve un token
    // así el usuario no tiene que hacer login justo después de registrarse
    suspend fun register(nombre: String, email: String, password: String): Result<LoginResponse> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(nombre, email, password))
            }
            if (response.status.value in 200..299) {
                Result.success(response.body<LoginResponse>())
            } else {
                Result.failure(Exception("Error en el registro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtiene las parejas del usuario autenticado desde la RDS
// El token en el header identifica al usuario — no hace falta pasar el idUsuario
    suspend fun getParejasUsuario(): List<Pareja> {
        return try {
            ApiClient.http.get("${ApiClient.BASE_URL}/parejas").body()
        } catch (e: Exception) {
            // Si falla la llamada devolvemos lista vacía en lugar del mock
            emptyList()
        }
    }

    // Crea una nueva pareja en la RDS y vincula al usuario autenticado como USUARIO_1
// Devuelve Result.success con la pareja creada, o Result.failure si algo falla
    suspend fun crearPareja(nombrePareja: String): Result<Pareja> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/parejas") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("nombrePareja" to nombrePareja))
            }
            if (response.status.value in 200..299) {
                Result.success(response.body<Pareja>())
            } else {
                Result.failure(Exception("Error al crear la pareja"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    }

    // Genera un código de invitación único para una pareja
// El USUARIO_1 comparte este código con su pareja para que se una
    suspend fun generarCodigoInvitacion(idPareja: Int): Result<String> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/parejas/codigo") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("idPareja" to idPareja))
            }
            if (response.status.value in 200..299) {
                val body = response.body<Map<String, String>>()
                Result.success(body["codigo"] ?: "")
            } else {
                Result.failure(Exception("Error al generar el código"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Une al usuario autenticado a una pareja existente usando el código de invitación
// El código se invalida tras su uso para evitar que más de 2 usuarios se unan
    suspend fun unirseConCodigo(codigo: String): Result<Pareja> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/parejas/unirse") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("codigo" to codigo))
            }
            if (response.status.value in 200..299) {
                Result.success(response.body<Pareja>())
            } else {
                Result.failure(Exception("Código no válido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun guardarGasto(
        idPareja: Int,
        tituloGasto: String,
        cantidadTotal: Double,
        modoReparto: String = "MITAD",
        comentario: String = "",
        idCategoria: Int = 1,
        porcentajeUsuario1: Double? = null,
        porcentajeUsuario2: Double? = null,
        importeUsuario1: Double? = null,
        importeUsuario2: Double? = null
    ): Result<GastoResponse> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/gastos") {
                contentType(ContentType.Application.Json)
                setBody(GuardarGastoRequest(
                    idPareja = idPareja,
                    tituloGasto = tituloGasto,
                    cantidadTotal = cantidadTotal,
                    modoReparto = modoReparto,
                    comentario = comentario,
                    idCategoria = idCategoria,
                    porcentajeUsuario1 = porcentajeUsuario1,
                    porcentajeUsuario2 = porcentajeUsuario2,
                    importeUsuario1 = importeUsuario1,
                    importeUsuario2 = importeUsuario2
                ))
            }
            if (response.status.value in 200..299) {
                Result.success(response.body<GastoResponse>())
            } else {
                Result.failure(Exception("Error al guardar el gasto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtiene todos los gastos de una pareja ordenados por fecha
// Se usa idPareja como query parameter en la URL: GET /gastos?idPareja=1
    suspend fun getGastos(idPareja: Int): List<Gasto> {
        return try {
            ApiClient.http.get("${ApiClient.BASE_URL}/gastos") {
                parameter("idPareja", idPareja)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }
}