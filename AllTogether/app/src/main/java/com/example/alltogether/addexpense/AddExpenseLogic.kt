package com.example.alltogether.addexpense

data class ResultadoValidacionAddExpense(
    val esValido: Boolean,
    val mensaje: String
)

data class ResultadoRepartoAddExpense(
    val porcentajeUsuario1: Int,
    val porcentajeUsuario2: Int,
    val modoReparto: String
)

fun validarTituloGasto(titulo: String): ResultadoValidacionAddExpense {
    return if (titulo.isBlank()) {
        ResultadoValidacionAddExpense(
            esValido = false,
            mensaje = "Introduce un título para el gasto"
        )
    } else {
        ResultadoValidacionAddExpense(
            esValido = true,
            mensaje = ""
        )
    }
}

fun validarCantidadGasto(cantidadTexto: String): ResultadoValidacionAddExpense {
    val cantidad = cantidadTexto.toDoubleOrNull()

    return if (cantidad == null || cantidad <= 0) {
        ResultadoValidacionAddExpense(
            esValido = false,
            mensaje = "Introduce una cantidad válida"
        )
    } else {
        ResultadoValidacionAddExpense(
            esValido = true,
            mensaje = ""
        )
    }
}

fun validarRepartoGasto(
    porcentajeYoTexto: String,
    porcentajeOtroTexto: String
): ResultadoValidacionAddExpense {
    val porcentajeYo = porcentajeYoTexto.toIntOrNull()
    val porcentajeOtro = porcentajeOtroTexto.toIntOrNull()

    if (porcentajeYo == null || porcentajeOtro == null) {
        return ResultadoValidacionAddExpense(
            esValido = false,
            mensaje = "Introduce un reparto válido"
        )
    }

    if (porcentajeYo < 0 || porcentajeOtro < 0) {
        return ResultadoValidacionAddExpense(
            esValido = false,
            mensaje = "Los porcentajes no pueden ser negativos"
        )
    }

    if (porcentajeYo + porcentajeOtro != 100) {
        return ResultadoValidacionAddExpense(
            esValido = false,
            mensaje = "Los porcentajes deben sumar 100"
        )
    }

    return ResultadoValidacionAddExpense(
        esValido = true,
        mensaje = ""
    )
}

fun calcularRepartoFinalAddExpense(
    rolYo: String,
    porcentajeYo: Int,
    porcentajeOtro: Int
): ResultadoRepartoAddExpense {
    val porcentajeUsuario1Final = if (rolYo == "USUARIO_1") {
        porcentajeYo
    } else {
        porcentajeOtro
    }

    val porcentajeUsuario2Final = if (rolYo == "USUARIO_1") {
        porcentajeOtro
    } else {
        porcentajeYo
    }

    val modoRepartoFinal =
        if (porcentajeUsuario1Final == 50 && porcentajeUsuario2Final == 50) {
            "MITAD"
        } else {
            "PORCENTAJE"
        }

    return ResultadoRepartoAddExpense(
        porcentajeUsuario1 = porcentajeUsuario1Final,
        porcentajeUsuario2 = porcentajeUsuario2Final,
        modoReparto = modoRepartoFinal
    )
}

fun calcularPorcentajeContrarioAddExpense(porcentaje: Int): Int {
    return 100 - porcentaje
}

fun puedePulsarGuardarGasto(cargando: Boolean): Boolean {
    return !cargando
}

fun obtenerMensajeGuardarGastoError(): String {
    return "No se pudo guardar el gasto"
}

fun debeMostrarMensajeAddExpense(mensaje: String): Boolean {
    return mensaje.isNotBlank()
}

