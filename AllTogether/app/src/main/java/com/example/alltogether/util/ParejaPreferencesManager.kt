package com.example.alltogether.util

import android.content.Context
import android.preference.PreferenceManager
import com.example.alltogether.R

class ParejaPreferencesManager(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun guardarNombreVisible(idPareja: Int, nombre: String) {
        prefs.edit()
            .putString("nombre_visible_pareja_$idPareja", nombre)
            .apply()
    }

    fun obtenerNombreVisible(idPareja: Int, nombrePorDefecto: String): String {
        return prefs.getString("nombre_visible_pareja_$idPareja", nombrePorDefecto)
            ?: nombrePorDefecto
    }

    fun guardarIconoPareja(idPareja: Int, nombreIcono: String) {
        prefs.edit()
            .putString("icono_pareja_nombre_$idPareja", nombreIcono)
            .apply()
    }

    fun obtenerNombreIconoPareja(idPareja: Int): String {
        return prefs.getString("icono_pareja_nombre_$idPareja", "corazon")
            ?: "corazon"
    }

    fun obtenerIconoParejaResId(idPareja: Int): Int {
        val nombreIcono = obtenerNombreIconoPareja(idPareja)
        return nombreIconoAResId(nombreIcono)
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