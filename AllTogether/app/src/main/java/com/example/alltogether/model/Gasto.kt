package com.example.alltogether.model

import kotlinx.serialization.Serializable
import java.io.Serializable as JSerializable

@Serializable
data class GastoResponse(
    val idGasto: Int,
    val tituloGasto: String,
    val cantidadTotal: Double,
    val modoReparto: String,
    val importeUsuario1: Double,
    val importeUsuario2: Double
)


@Serializable
data class GastosPaginados(
    val gastos: List<Gasto>,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val hayMas: Boolean
)

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
    val pagadoUsuario2: Boolean,
    val nombreCategoria: String = "",
    val tipoGasto: String = ""
) : JSerializable