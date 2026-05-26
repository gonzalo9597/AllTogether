package com.example.alltogether.addcouple

data class ResultadoValidacionAddCouple(
    val esValido: Boolean,
    val mensaje: String
)

fun validarNombrePareja(nombrePareja: String): ResultadoValidacionAddCouple {
    return if (nombrePareja.isBlank()) {
        ResultadoValidacionAddCouple(
            esValido = false,
            mensaje = "Introduce un nombre para la pareja"
        )
    } else {
        ResultadoValidacionAddCouple(
            esValido = true,
            mensaje = ""
        )
    }
}

fun validarCodigoInvitacion(codigo: String): ResultadoValidacionAddCouple {
    return if (codigo.isBlank()) {
        ResultadoValidacionAddCouple(
            esValido = false,
            mensaje = "Introduce un código de invitación"
        )
    } else {
        ResultadoValidacionAddCouple(
            esValido = true,
            mensaje = ""
        )
    }
}

fun obtenerMensajeCrearParejaCorrecto(): String {
    return "Pareja creada correctamente"
}

fun obtenerMensajeCrearParejaError(): String {
    return "No se pudo crear la pareja"
}

fun obtenerMensajeUnirseCorrecto(): String {
    return "Te has unido a la pareja correctamente"
}

fun obtenerMensajeUnirseError(): String {
    return "Código no válido o ya utilizado"
}

fun puedePulsarBotonCrear(cargando: Boolean): Boolean {
    return !cargando
}

fun puedePulsarBotonUnirse(cargandoUnirse: Boolean): Boolean {
    return !cargandoUnirse
}

fun limpiarNombrePareja(): String {
    return ""
}

fun limpiarCodigoInvitacion(): String {
    return ""
}

fun debeMostrarMensaje(mensaje: String): Boolean {
    return mensaje.isNotBlank()
}

fun ocultarCardInformativa(): Boolean {
    return false
}

