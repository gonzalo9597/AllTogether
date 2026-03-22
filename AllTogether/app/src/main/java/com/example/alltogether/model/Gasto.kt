package com.example.alltogether.model

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
    val porcentajeUsuario1: Double?,
    val porcentajeUsuario2: Double?,
    val importeUsuario1: Double?,
    val importeUsuario2: Double?,
    val pagadoUsuario1: Boolean,
    val pagadoUsuario2: Boolean
)