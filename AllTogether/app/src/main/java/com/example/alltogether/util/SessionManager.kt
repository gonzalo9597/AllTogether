package com.example.alltogether.util

import android.content.Context
import android.preference.PreferenceManager

class SessionManager(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun saveSession(userId: Int, userName: String) {
        prefs.edit()
            .putBoolean("logged_in", true)
            .putInt("user_id", userId)
            .putString("user_name", userName)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean("logged_in", false)
            .remove("user_id")
            .remove("user_name")
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("logged_in", false)
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun getUserName(): String {
        return prefs.getString("user_name", "") ?: ""
    }
}