package com.example.alltogether.network

import com.example.alltogether.model.Pareja
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import com.example.alltogether.model.GastoResponse
import com.example.alltogether.model.Gasto
import com.example.alltogether.model.GastoRecurrente
import io.ktor.client.statement.bodyAsText

// Estructura de la respuesta que devuelve el servidor tras login/register
// @Serializable permite que Ktor convierta automáticamente el JSON a este objeto
@Serializable
data class LoginResponse(
    val token: String,      // El JWT que usaremos como "DNI" en futuras peticiones
    val idUsuario: Int,     // ID del usuario en la base de datos
    val nombre: String,     // Nombre del usuario
    val email: String       // Email del usuario
)

@Serializable
data class EditarGastoRecurrenteRequest(
    val idRecurrente: Int,
    val tituloGasto: String,
    val cantidadTotal: Double,
    val frecuencia: String,
    val diaDelMes: Int,
    val idCategoria: Int,
    val modoReparto: String = "MITAD",
    val comentario: String = "",
    val porcentajeUsuario1: Double? = null,
    val porcentajeUsuario2: Double? = null
)

// Lo que enviamos al servidor para hacer login
@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class SaldarDeudaRequest(
    val idGasto: Int,
    val rolPagado: String? = null
)

// Lo que enviamos al servidor para registrarse
@Serializable
data class RegisterRequest(val nombre: String, val email: String, val password: String)

// Data class para editar un gasto — evita el problema de tipos mixtos en el Map
@Serializable
data class EditarGastoRequest(
    val idGasto: Int,
    val tituloGasto: String,
    val cantidadTotal: Double,
    val comentario: String = "",
    val idCategoria: Int = 1,
    val modoReparto: String = "MITAD",
    val porcentajeUsuario1: Double? = null,
    val porcentajeUsuario2: Double? = null
)

@Serializable
data class SaldarDeudaGlobalRequest(
    val idPareja: Int,
    val rolDeudor: String
)

// Datos que enviamos al servidor para guardar un gasto
@Serializable
data class GuardarGastoRequest(
    val idPareja: Int,
    val tituloGasto: String,
    val cantidadTotal: Double,
    val modoReparto: String = "MITAD",
    val comentario: String = "",
    val fechaGasto: String = "",
    val pagadoPor: String = "",
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
        fechaGasto: String = "",
        pagadoPor: String = "",
        idCategoria: Int = 1,
        porcentajeUsuario1: Double? = null,
        porcentajeUsuario2: Double? = null,
        importeUsuario1: Double? = null,
        importeUsuario2: Double? = null
    ): Result<GastoResponse> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/gastos") {
                contentType(ContentType.Application.Json)
                setBody(
                    GuardarGastoRequest(
                        idPareja = idPareja,
                        tituloGasto = tituloGasto,
                        cantidadTotal = cantidadTotal,
                        modoReparto = modoReparto,
                        comentario = comentario,
                        fechaGasto = fechaGasto,
                        pagadoPor = pagadoPor,
                        idCategoria = idCategoria,
                        porcentajeUsuario1 = porcentajeUsuario1,
                        porcentajeUsuario2 = porcentajeUsuario2,
                        importeUsuario1 = importeUsuario1,
                        importeUsuario2 = importeUsuario2
                    )
                )
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
    // Marca la parte del usuario autenticado como pagada en un gasto
// Si ambos usuarios pagan, el gasto se marca automáticamente como no pendiente
    suspend fun saldarDeuda(idGasto: Int, rolPagado: String? = null): Result<String> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/gastos/saldar") {
                contentType(ContentType.Application.Json)
                setBody(
                    SaldarDeudaRequest(
                        idGasto = idGasto,
                        rolPagado = rolPagado
                    )
                )
            }

            if (response.status.value in 200..299) {
                Result.success("Pago registrado correctamente")
            } else {
                Result.failure(Exception("Error al registrar el pago"))
            }
        } catch (e: Exception) {
            android.util.Log.e("SALDAR_DEUDA", "Error: ${e.message}")
            Result.failure(e)
        }
    }
    // Elimina al usuario autenticado de una pareja
// Si era el último miembro, la pareja se marca como inactiva en la RDS
    suspend fun abandonarPareja(idPareja: Int): Result<String> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/parejas/abandonar") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("idPareja" to idPareja))
            }
            if (response.status.value in 200..299) {
                Result.success("Has abandonado la pareja correctamente")
            } else {
                Result.failure(Exception("Error al abandonar la pareja"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Marca la cuenta del usuario como inactiva en la RDS
// Se conserva el historial de gastos por integridad de datos
// El usuario es eliminado de todas sus parejas automáticamente
    suspend fun eliminarCuenta(): Result<String> {
        return try {
            val response = ApiClient.http.delete("${ApiClient.BASE_URL}/usuario")
            if (response.status.value in 200..299) {
                Result.success("Cuenta eliminada correctamente")
            } else {
                Result.failure(Exception("Error al eliminar la cuenta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Data class para crear un gasto recurrente
    @Serializable
    data class GastoRecurrenteRequest(
        val idPareja: Int,
        val tituloGasto: String,
        val cantidadTotal: Double,
        val frecuencia: String = "MENSUAL",
        val diaDelMes: Int = 1,
        val idCategoria: Int = 1,
        val modoReparto: String = "MITAD",
        val comentario: String = "",
        val porcentajeUsuario1: Double? = null,
        val porcentajeUsuario2: Double? = null
    )

    // Respuesta del servidor con el balance entre los dos usuarios de la pareja
    @Serializable
    data class Balance(
        val miDeuda: Double,
        val deudaOtro: Double,
        val diferencia: Double,
        val nombreYo: String,
        val nombreOtro: String,
        val mensaje: String,
        val rolUsuario: String
    )

    // Crea una plantilla de gasto recurrente en la RDS
// AWS EventBridge se encargará de generar el gasto automáticamente cada vez que toque
    suspend fun crearGastoRecurrente(
        idPareja: Int,
        tituloGasto: String,
        cantidadTotal: Double,
        frecuencia: String = "MENSUAL",
        diaDelMes: Int = 1,
        idCategoria: Int = 1,
        modoReparto: String = "MITAD",
        comentario: String = "",
        porcentajeUsuario1: Double? = null,
        porcentajeUsuario2: Double? = null
    ): Result<String> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/recurrentes") {
                contentType(ContentType.Application.Json)
                setBody(
                    GastoRecurrenteRequest(
                        idPareja = idPareja,
                        tituloGasto = tituloGasto,
                        cantidadTotal = cantidadTotal,
                        frecuencia = frecuencia,
                        diaDelMes = diaDelMes,
                        idCategoria = idCategoria,
                        modoReparto = modoReparto,
                        comentario = comentario,
                        porcentajeUsuario1 = porcentajeUsuario1,
                        porcentajeUsuario2 = porcentajeUsuario2
                    )
                )
            }
            if (response.status.value in 200..299) {
                Result.success("Gasto recurrente creado correctamente")
            } else {
                Result.failure(Exception("Error al crear el gasto recurrente"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Envía el token FCM del dispositivo al servidor para recibir notificaciones push
// Se llama automáticamente cuando Firebase genera un token nuevo
    suspend fun guardarTokenFCM(fcmToken: String): Result<String> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/usuario/fcm-token") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("fcmToken" to fcmToken))
            }
            if (response.status.value in 200..299) {
                Result.success("Token FCM guardado correctamente")
            } else {
                Result.failure(Exception("Error al guardar el token FCM"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Actualiza el título, cantidad y comentario de un gasto existente
// El modo de reparto no cambia — solo se recalculan los importes con la nueva cantidad
    suspend fun editarGasto(
        idGasto: Int,
        tituloGasto: String,
        cantidadTotal: Double,
        comentario: String = "",
        idCategoria: Int = 1,
        modoReparto: String = "MITAD",
        porcentajeUsuario1: Double? = null,
        porcentajeUsuario2: Double? = null
    ): Result<String> {
        return try {
            android.util.Log.d("EDITAR_GASTO", "Editando gasto $idGasto: $tituloGasto, $cantidadTotal")
            val response = ApiClient.http.put("${ApiClient.BASE_URL}/gastos") {
                contentType(ContentType.Application.Json)
                setBody(
                    EditarGastoRequest(
                        idGasto = idGasto,
                        tituloGasto = tituloGasto,
                        cantidadTotal = cantidadTotal,
                        comentario = comentario,
                        idCategoria = idCategoria,
                        modoReparto = modoReparto,
                        porcentajeUsuario1 = porcentajeUsuario1,
                        porcentajeUsuario2 = porcentajeUsuario2
                    )
                )
            }
            android.util.Log.d("EDITAR_GASTO", "Respuesta: ${response.status.value}")
            if (response.status.value in 200..299) {
                Result.success("Gasto actualizado correctamente")
            } else {
                Result.failure(Exception("Error al actualizar el gasto"))
            }
        } catch (e: Exception) {
            android.util.Log.e("EDITAR_GASTO", "Error: ${e.message}")
            Result.failure(e)
        }
    }
    // Elimina un gasto de la RDS
// Solo puede eliminar gastos de parejas a las que pertenece el usuario
    suspend fun eliminarGasto(idGasto: Int): Result<String> {
        return try {
            val response = ApiClient.http.delete("${ApiClient.BASE_URL}/gastos") {
                parameter("idGasto", idGasto)
            }
            if (response.status.value in 200..299) {
                Result.success("Gasto eliminado correctamente")
            } else {
                Result.failure(Exception("Error al eliminar el gasto"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecurrentes(idPareja: Int): List<GastoRecurrente> {
        return try {
            ApiClient.http.get("${ApiClient.BASE_URL}/recurrentes") {
                parameter("idPareja", idPareja)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun editarGastoRecurrente(
        idRecurrente: Int,
        tituloGasto: String,
        cantidadTotal: Double,
        frecuencia: String,
        diaDelMes: Int,
        idCategoria: Int,
        modoReparto: String = "MITAD",
        comentario: String = "",
        porcentajeUsuario1: Double? = null,
        porcentajeUsuario2: Double? = null
    ): Result<String> {
        return try {
            val response = ApiClient.http.put("${ApiClient.BASE_URL}/recurrentes") {
                contentType(ContentType.Application.Json)
                setBody(
                    EditarGastoRecurrenteRequest(
                        idRecurrente = idRecurrente,
                        tituloGasto = tituloGasto,
                        cantidadTotal = cantidadTotal,
                        frecuencia = frecuencia,
                        diaDelMes = diaDelMes,
                        idCategoria = idCategoria,
                        modoReparto = modoReparto,
                        comentario = comentario,
                        porcentajeUsuario1 = porcentajeUsuario1,
                        porcentajeUsuario2 = porcentajeUsuario2
                    )
                )
            }

            if (response.status.value in 200..299) {
                Result.success("Gasto recurrente actualizado correctamente")
            } else {
                Result.failure(Exception("Error al editar el gasto recurrente"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // Obtiene el balance entre los dos usuarios de una pareja
// Devuelve quién le debe a quién y cuánto
    suspend fun getBalance(idPareja: Int): Result<Balance> {
        return try {
            val response = ApiClient.http.get("${ApiClient.BASE_URL}/balance") {
                parameter("idPareja", idPareja)
            }
            if (response.status.value in 200..299) {
                Result.success(response.body<Balance>())
            } else {
                Result.failure(Exception("Error al obtener el balance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saldarDeudaGlobal(
        idPareja: Int,
        rolDeudor: String
    ): Result<String> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/gastos/saldar") {
                contentType(ContentType.Application.Json)
                setBody(
                    SaldarDeudaGlobalRequest(
                        idPareja = idPareja,
                        rolDeudor = rolDeudor
                    )
                )
            }

            if (response.status.value in 200..299) {
                Result.success("Deuda saldada correctamente")
            } else {
                Result.failure(Exception("Error al saldar la deuda global"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun actualizarPerfil(nombre: String): Result<String> {
        return try {
            val response = ApiClient.http.put("${ApiClient.BASE_URL}/usuario/perfil") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("nombre" to nombre))
            }
            if (response.status.value in 200..299) {
                Result.success("Perfil actualizado correctamente")
            } else {
                Result.failure(Exception("Error al actualizar el perfil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun actualizarDivisa(divisa: String): Result<String> {
        return try {
            val response = ApiClient.http.put("${ApiClient.BASE_URL}/usuario/divisa") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("divisa" to divisa))
            }
            if (response.status.value in 200..299) {
                Result.success("Divisa actualizada")
            } else {
                Result.failure(Exception("Error al actualizar la divisa"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun loginGoogle(idToken: String): Result<LoginResponse> {
        return try {
            val response = ApiClient.http.post("${ApiClient.BASE_URL}/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("idToken" to idToken))
            }
            android.util.Log.d("GoogleAuth", "Lambda status: ${response.status.value}")
            if (response.status.value in 200..299) {
                Result.success(response.body<LoginResponse>())
            } else {
                val bodyText = response.bodyAsText()
                android.util.Log.e("GoogleAuth", "Lambda error body: $bodyText")
                Result.failure(Exception("Error al iniciar sesión con Google"))
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleAuth", "Excepcion HTTP: ${e.message}", e)
            Result.failure(e)
        }
    }
}
