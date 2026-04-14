package com.example.alltogether.util

import android.content.Context
import com.example.alltogether.R
import java.io.File

class ParejaPreferencesManager(private val context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("pareja_prefs", Context.MODE_PRIVATE)

    fun guardarNombreVisible(idPareja: Int, nombreVisible: String) {
        sharedPreferences.edit()
            .putString("nombre_visible_pareja_$idPareja", nombreVisible)
            .apply()
    }

    fun obtenerNombreVisible(idPareja: Int, nombreParejaOriginal: String): String {
        return sharedPreferences.getString(
            "nombre_visible_pareja_$idPareja",
            nombreParejaOriginal
        ) ?: nombreParejaOriginal
    }

    fun guardarIconoPareja(idPareja: Int, nombreIcono: String) {
        val rutaAnterior = obtenerImagenPersonalizadaPareja(idPareja)
        if (!rutaAnterior.isNullOrBlank()) {
            runCatching { File(rutaAnterior).delete() }
        }

        sharedPreferences.edit()
            .putString("icono_pareja_nombre_$idPareja", nombreIcono)
            .remove("imagen_pareja_$idPareja")
            .apply()
    }

    fun guardarImagenPersonalizadaPareja(idPareja: Int, rutaImagen: String) {
        val rutaAnterior = obtenerImagenPersonalizadaPareja(idPareja)

        if (!rutaAnterior.isNullOrBlank() && rutaAnterior != rutaImagen) {
            runCatching { File(rutaAnterior).delete() }
        }

        sharedPreferences.edit()
            .putString("imagen_pareja_$idPareja", rutaImagen)
            .apply()
    }

    fun obtenerImagenPersonalizadaPareja(idPareja: Int): String? {
        return sharedPreferences.getString("imagen_pareja_$idPareja", null)
    }

    fun limpiarImagenPersonalizadaPareja(idPareja: Int) {
        val rutaAnterior = obtenerImagenPersonalizadaPareja(idPareja)
        if (!rutaAnterior.isNullOrBlank()) {
            runCatching { File(rutaAnterior).delete() }
        }

        sharedPreferences.edit()
            .remove("imagen_pareja_$idPareja")
            .apply()
    }

    fun obtenerNombreIconoPareja(idPareja: Int): String {
        return sharedPreferences.getString(
            "icono_pareja_nombre_$idPareja",
            "corazon"
        ) ?: "corazon"
    }

    fun obtenerIconoParejaResId(idPareja: Int): Int {
        return nombreIconoAResId(obtenerNombreIconoPareja(idPareja))
    }

    fun nombreIconoAResId(nombreIcono: String): Int {
        return when (nombreIcono) {
            "corazon" -> R.drawable.corazon
            "casa" -> R.drawable.casa
            "pareja" -> R.drawable.pareja
            "viaje" -> R.drawable.viaje
            "cafe" -> R.drawable.cafe
            "gato" -> R.drawable.gato
            else -> R.drawable.corazon
        }
    }
}