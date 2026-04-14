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
            "JPY" -> "¥"
            else  -> "€"
        }
    }
    fun obtenerTasaCambio(): Double {
        return when (obtenerDivisa()) {
            "EUR" -> 1.0
            "USD" -> 1.08   // 1 EUR = 1.08 USD (actualiza según necesites)
            "JPY" -> 161.0  // 1 EUR = 161 JPY
            else  -> 1.0
        }
    }

    fun convertir(cantidad: Double): Double {
        return cantidad * obtenerTasaCambio()
    }
}