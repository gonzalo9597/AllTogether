package com.example.alltogether.model

import kotlinx.serialization.Serializable

// @Serializable permite que Ktor convierta automáticamente el JSON de la API a este objeto
// Los nombres de los campos deben coincidir exactamente con los que devuelve el backend
@Serializable
data class Pareja(
    val idPareja: Int,
    val nombrePareja: String,
    val imagenUrl: String = "" // Opcional — vacío por ahora hasta implementar imágenes
)