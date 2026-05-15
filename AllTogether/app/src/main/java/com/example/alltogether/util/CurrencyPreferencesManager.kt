package com.example.alltogether.util

import android.content.Context
import android.preference.PreferenceManager

class CurrencyPreferencesManager(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun guardarDivisa(divisa: String) {
        prefs.edit().putString("divisa_seleccionada", divisa).apply()
    }

    fun obtenerDivisa(): String {
        return prefs.getString("divisa_seleccionada", "EUR") ?: "EUR"
    }

    fun obtenerSimbolo(): String {
        return when (obtenerDivisa()) {
            "EUR" -> "€"
            "USD" -> "$"
            "GBP" -> "£"
            "JPY" -> "¥"
            "CHF" -> "₣"
            "MXN" -> "MX$"
            else  -> "€"
        }
    }

    fun obtenerTasaCambio(): Double {
        return when (obtenerDivisa()) {
            "EUR" -> 1.0
            "USD" -> 1.08
            "GBP" -> 0.85
            "JPY" -> 161.0
            "CHF" -> 0.97
            "MXN" -> 19.5
            else  -> 1.0
        }
    }

    // Convierte de la divisa actual a EUR para guardar en la RDS
    fun aEuros(cantidad: Double): Double {
        return cantidad / obtenerTasaCambio()
    }

    // Convierte de EUR a la divisa actual para mostrar al usuario
    fun convertir(cantidad: Double): Double {
        return cantidad * obtenerTasaCambio()
    }

    // Formatea un importe con el símbolo correcto
    fun formatear(cantidad: Double): String {
        val convertido = convertir(cantidad)
        val simbolo = obtenerSimbolo()
        return if (obtenerDivisa() == "JPY") {
            "$simbolo${convertido.toLong()}"
        } else {
            "$simbolo${"%.2f".format(convertido)}"
        }
    }

    // Lista de divisas disponibles
    val divisas = listOf("EUR", "USD", "GBP", "JPY", "CHF", "MXN")
}