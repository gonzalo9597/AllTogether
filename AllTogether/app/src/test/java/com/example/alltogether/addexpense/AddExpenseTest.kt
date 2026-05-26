package com.example.alltogether.addexpense

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddExpenseTest {

    @Test
    fun validarTituloGasto_conTituloVacio_devuelveError() {
        val resultado = validarTituloGasto("")

        assertFalse(resultado.esValido)
        assertEquals("Introduce un título para el gasto", resultado.mensaje)
    }

    @Test
    fun validarTituloGasto_conTituloCorrecto_devuelveValido() {
        val resultado = validarTituloGasto("Supermercado")

        assertTrue(resultado.esValido)
        assertEquals("", resultado.mensaje)
    }

    @Test
    fun validarCantidadGasto_conTextoNoNumerico_devuelveError() {
        val resultado = validarCantidadGasto("abc")

        assertFalse(resultado.esValido)
        assertEquals("Introduce una cantidad válida", resultado.mensaje)
    }

    @Test
    fun validarCantidadGasto_conCantidadCero_devuelveError() {
        val resultado = validarCantidadGasto("0")

        assertFalse(resultado.esValido)
        assertEquals("Introduce una cantidad válida", resultado.mensaje)
    }

    @Test
    fun validarCantidadGasto_conCantidadCorrecta_devuelveValido() {
        val resultado = validarCantidadGasto("24.50")

        assertTrue(resultado.esValido)
        assertEquals("", resultado.mensaje)
    }

    @Test
    fun validarRepartoGasto_conPorcentajesNoNumericos_devuelveError() {
        val resultado = validarRepartoGasto("abc", "50")

        assertFalse(resultado.esValido)
        assertEquals("Introduce un reparto válido", resultado.mensaje)
    }

    @Test
    fun validarRepartoGasto_conPorcentajesQueNoSuman100_devuelveError() {
        val resultado = validarRepartoGasto("60", "30")

        assertFalse(resultado.esValido)
        assertEquals("Los porcentajes deben sumar 100", resultado.mensaje)
    }

    @Test
    fun calcularRepartoFinalAddExpense_conUsuario1YReparto6040_colocaBienLosPorcentajes() {
        val resultado = calcularRepartoFinalAddExpense(
            rolYo = "USUARIO_1",
            porcentajeYo = 60,
            porcentajeOtro = 40
        )

        assertEquals(60, resultado.porcentajeUsuario1)
        assertEquals(40, resultado.porcentajeUsuario2)
        assertEquals("PORCENTAJE", resultado.modoReparto)
    }

    @Test
    fun calcularRepartoFinalAddExpense_conUsuario2YReparto6040_colocaBienLosPorcentajes() {
        val resultado = calcularRepartoFinalAddExpense(
            rolYo = "USUARIO_2",
            porcentajeYo = 60,
            porcentajeOtro = 40
        )

        assertEquals(40, resultado.porcentajeUsuario1)
        assertEquals(60, resultado.porcentajeUsuario2)
        assertEquals("PORCENTAJE", resultado.modoReparto)
    }

    @Test
    fun calcularRepartoFinalAddExpense_conReparto5050_devuelveModoMitad() {
        val resultado = calcularRepartoFinalAddExpense(
            rolYo = "USUARIO_1",
            porcentajeYo = 50,
            porcentajeOtro = 50
        )

        assertEquals(50, resultado.porcentajeUsuario1)
        assertEquals(50, resultado.porcentajeUsuario2)
        assertEquals("MITAD", resultado.modoReparto)
    }
}