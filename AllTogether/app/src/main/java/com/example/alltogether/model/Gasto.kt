package com.example.alltogether.model

import kotlinx.serialization.Serializable

// Representa la respuesta del servidor al guardar un gasto
// Contiene los importes calculados según el modo de reparto
@Serializable
data class GastoResponse(
    val idGasto: Int,
    val tituloGasto: String,
    val cantidadTotal: Double,
    val modoReparto: String,
    val importeUsuario1: Double,
    val importeUsuario2: Double
)

// Representa un gasto completo — se usará cuando implementemos getGastos
@Serializable
data class Gasto(
    val idGasto: Int,
    val idPareja: Int,
    val idCategoria: Int,
    val idUsuarioCreador: Int,
    val tituloGasto: String,
    val modoReparto: String,
    val cantidadTotal: Double,
    val fechaGasto: String,
    val comentario: String,
    val esPendiente: Boolean,
    val porcentajeUsuario1: Double? = null,
    val porcentajeUsuario2: Double? = null,
    val importeUsuario1: Double? = null,
    val importeUsuario2: Double? = null,
    val pagadoUsuario1: Boolean,
    val pagadoUsuario2: Boolean
)