package com.freewdcmkt.bck.util

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {
    private lateinit var appContext: Context
    private lateinit var masterKey: MasterKey
    private lateinit var sharedPreferences: EncryptedSharedPreferences
    fun init(context: Context) {
        try {
            appContext = context.applicationContext
            masterKey =
                MasterKey.Builder(TokenManager.appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setUserAuthenticationRequired(false)
                    .build()
            TokenManager.sharedPreferences =
                EncryptedSharedPreferences.create(
                    TokenManager.appContext,
                    "secure_prefs",
                    TokenManager.masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ) as EncryptedSharedPreferences
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveToken(token: String) {
        sharedPreferences.edit { putString("token", token) }
    }

    fun getToken(): String? {
        return sharedPreferences.getString("token",null)
    }
}