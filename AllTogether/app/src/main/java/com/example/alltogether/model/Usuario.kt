package com.example.alltogether.model

data class Usuario(
    val idUsuario: Int,
    val nombreUsuario: String,
    val email: String,
    val rol: String,
    val activo: Boolean
)