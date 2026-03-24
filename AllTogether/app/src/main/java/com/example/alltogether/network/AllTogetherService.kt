package com.example.alltogether.network

import android.util.Log
import com.example.alltogether.model.Pareja
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class AllTogetherService {

    // Login de prueba
    suspend fun login(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }

    // Registro de prueba
    suspend fun register(nombre: String, email: String, password: String): Boolean {
        return nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()
    }

    // Obtener parejas del usuario
    // De momento el idUsuario aún no se usa, pero lo dejamos porque luego sí hará falta
    suspend fun getParejasUsuario(idUsuario: Int): List<Pareja> {
        var conexion: HttpURLConnection? = null

        return try {
            val url = URL("${ApiClient.BASE_URL}/alltogether-saludo")
            conexion = url.openConnection() as HttpURLConnection
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 10000
            conexion.readTimeout = 10000

            val codigoRespuesta = conexion.responseCode

            if (codigoRespuesta == HttpURLConnection.HTTP_OK) {
                val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
                Log.d("AWS_API", "Respuesta API: $respuesta")

                convertirJsonAParejas(respuesta)
            } else {
                Log.e("AWS_API", "Error HTTP: $codigoRespuesta")
                obtenerParejasMock()
            }

        } catch (e: Exception) {
            Log.e("AWS_API", "Error al llamar a AWS", e)
            obtenerParejasMock()
        } finally {
            conexion?.disconnect()
        }
    }

    // Crear pareja de prueba
    suspend fun crearPareja(nombrePareja: String): Boolean {
        return nombrePareja.isNotBlank()
    }

    // Unirse con código de prueba
    suspend fun unirseConCodigo(codigo: String): Boolean {
        return codigo.isNotBlank()
    }

    // Guardar gasto de prueba
    suspend fun guardarGasto(
        idPareja: Int,
        idCategoria: Int,
        idUsuarioCreador: Int,
        titulo: String,
        modoReparto: String,
        cantidad: Double,
        comentario: String
    ): Boolean {
        return titulo.isNotBlank() && cantidad > 0
    }

    // Convierte el texto JSON en una lista de objetos Pareja
    private fun convertirJsonAParejas(respuesta: String): List<Pareja> {
        val jsonArray = JSONArray(respuesta)
        val listaParejas = mutableListOf<Pareja>()

        for (i in 0 until jsonArray.length()) {
            val objeto = jsonArray.getJSONObject(i)

            val pareja = Pareja(
                idPareja = objeto.getInt("idPareja"),
                nombrePareja = objeto.getString("nombrePareja"),
                imagenUrl = objeto.optString("imagenUrl", "")
            )

            listaParejas.add(pareja)
        }

        return listaParejas
    }

    // Lista de respaldo por si falla AWS
    private fun obtenerParejasMock(): List<Pareja> {
        return listOf(
            Pareja(1, "Sergio y Ana"),
            Pareja(2, "Sergio y Marta")
        )
    }
}