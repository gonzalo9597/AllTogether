package com.example.alltogether.addcouple

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddCoupleTest {

    @Test
    fun validarNombrePareja_conNombreVacio_devuelveError() {
        val resultado = validarNombrePareja("")

        assertFalse(resultado.esValido)
        assertEquals("Introduce un nombre para la pareja", resultado.mensaje)
    }

    @Test
    fun validarNombrePareja_conNombreCorrecto_devuelveValido() {
        val resultado = validarNombrePareja("Casa")

        assertTrue(resultado.esValido)
        assertEquals("", resultado.mensaje)
    }

    @Test
    fun validarCodigoInvitacion_conCodigoVacio_devuelveError() {
        val resultado = validarCodigoInvitacion("")

        assertFalse(resultado.esValido)
        assertEquals("Introduce un código de invitación", resultado.mensaje)
    }

    @Test
    fun validarCodigoInvitacion_conCodigoCorrecto_devuelveValido() {
        val resultado = validarCodigoInvitacion("ABC123")

        assertTrue(resultado.esValido)
        assertEquals("", resultado.mensaje)
    }

    @Test
    fun puedePulsarBotonCrear_cuandoNoEstaCargando_devuelveTrue() {
        val resultado = puedePulsarBotonCrear(cargando = false)

        assertTrue(resultado)
    }

    @Test
    fun puedePulsarBotonCrear_cuandoEstaCargando_devuelveFalse() {
        val resultado = puedePulsarBotonCrear(cargando = true)

        assertFalse(resultado)
    }

    @Test
    fun puedePulsarBotonUnirse_cuandoEstaCargando_devuelveFalse() {
        val resultado = puedePulsarBotonUnirse(cargandoUnirse = true)

        assertFalse(resultado)
    }

    @Test
    fun obtenerMensajeCrearParejaCorrecto_devuelveMensajeEsperado() {
        val resultado = obtenerMensajeCrearParejaCorrecto()

        assertEquals("Pareja creada correctamente", resultado)
    }

    @Test
    fun obtenerMensajeUnirseError_devuelveMensajeEsperado() {
        val resultado = obtenerMensajeUnirseError()

        assertEquals("Código no válido o ya utilizado", resultado)
    }

    @Test
    fun debeMostrarMensaje_conMensajeConTexto_devuelveTrue() {
        val resultado = debeMostrarMensaje("Pareja creada correctamente")

        assertTrue(resultado)
    }
}