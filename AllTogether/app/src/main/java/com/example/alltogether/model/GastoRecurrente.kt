package com.example.alltogether.model

import kotlinx.serialization.Serializable
import java.io.Serializable as JSerializable

@Serializable
data class GastoRecurrente(
    val idRecurrente: Int,
    val idPareja: Int,
    val idCategoria: Int,
    val idUsuarioCreador: Int,
    val tituloGasto: String,
    val modoReparto: String,
    val cantidadTotal: Double,
    val comentario: String,
    val frecuencia: String,
    val diaDelMes: Int,
    val fechaInicio: String,
    val fechaFin: String? = null,
    val activo: Boolean,
    val ultimaGeneracion: String? = null,
    val nombreCategoria: String = ""
) : JSerializable