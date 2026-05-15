package com.example.alltogether.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Gestiona la sesión del usuario — guarda y recupera los datos de autenticación
// Usa EncryptedSharedPreferences en lugar de SharedPreferences normal
// para cifrar los datos en disco (incluyendo el JWT token)
class SessionManager(context: Context) {

    // Clave maestra que se usa para cifrar los datos
    // AES256_GCM es el algoritmo de cifrado — estándar de seguridad moderno
    // Android Keystore gestiona esta clave de forma segura en el hardware del dispositivo
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Almacenamiento cifrado — tanto las claves como los valores están cifrados en disco
    // A diferencia de SharedPreferences normal, aunque alguien acceda al archivo
    // del dispositivo no podrá leer el token ni los datos del usuario
    // AES256_SIV cifra las claves, AES256_GCM cifra los valores
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "alltogether_session",  // Nombre del archivo de preferencias
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Guarda todos los datos de sesión tras un login o registro exitoso
    // token — el JWT que se enviará en cada petición a la API
    // userId — el ID del usuario en la base de datos
    // userName — el nombre del usuario para mostrar en la UI
    // email — el email del usuario
    fun saveSession(token: String, userId: Int, userName: String, email: String) {
        prefs.edit()
            .putString("jwt_token", token)
            .putInt("user_id", userId)
            .putString("user_name", userName)
            .putString("user_email", email)
            .apply() // apply() es asíncrono — no bloquea el hilo principal
    }

    // Elimina todos los datos de sesión — se llama al cerrar sesión
    fun clearSession() {
        prefs.edit().clear().apply()
    }
    fun updateUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    // Devuelve el email del usuario, o cadena vacía si no hay sesión
    fun getUserEmail(): String = prefs.getString("user_email", "") ?: ""

    // Devuelve el JWT token guardado, o null si no hay sesión
    fun getToken(): String? = prefs.getString("jwt_token", null)


    // Devuelve el nombre del usuario, o cadena vacía si no hay sesión
    fun getUserName(): String = prefs.getString("user_name", "") ?: ""

    // Comprueba si hay una sesión activa — simplemente verifica si existe un token guardado
    // Se usa al arrancar la app para decidir si ir a Login o a MisParejas directamente
    fun isLoggedIn(): Boolean = getToken() != null
}