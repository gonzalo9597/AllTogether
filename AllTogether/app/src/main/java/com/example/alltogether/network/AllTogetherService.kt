package com.example.alltogether.network

import com.example.alltogether.model.Pareja

class AllTogetherService {

    // Login de prueba, luego aquí conectaremos la API real
    suspend fun login(email: String, password: String): Boolean {
        return email.isNotBlank() && password.isNotBlank()
    }

    suspend fun register(nombre: String, email: String, password: String): Boolean {
        return nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()
    }

    suspend fun getParejasUsuario(idUsuario: Int): List<Pareja> {
        // Datos de prueba
        return listOf(
            Pareja(1, "Sergio y Ana"),
            Pareja(2, "Sergio y Marta")
        )
    }

    suspend fun crearPareja(nombrePareja: String): Boolean {
        return nombrePareja.isNotBlank()
    }

    suspend fun unirseConCodigo(codigo: String): Boolean {
        return codigo.isNotBlank()
    }

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
}