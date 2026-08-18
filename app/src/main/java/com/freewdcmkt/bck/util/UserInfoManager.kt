package com.freewdcmkt.bck.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_info")
val USERNAME = stringPreferencesKey("username")
val UID = stringPreferencesKey("uid")
val IS_LOGIN = booleanPreferencesKey("is_login")
val USER_ACCOUNT = stringPreferencesKey("user_account")
val HOME_IMAGE_URL = stringPreferencesKey("home_image_url")
val NOTIFICATION_ID = intPreferencesKey("notification_id")
val EXP = intPreferencesKey("exp")

object UserInfoManager {
    private lateinit var dataStore: DataStore<Preferences>
    fun init(context: Context) {
        dataStore = context.dataStore
    }

    suspend fun saveUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[USERNAME] = username
        }
    }

    fun getUsernameFlow() = dataStore.data
        .catch { exception ->
            emit(emptyPreferences())
        }
        .map { preferences ->
            preferences[USERNAME] ?: ""
        }

    suspend fun saveUid(uid: String) {
        dataStore.edit { preferences ->
            preferences[UID] = uid
        }
    }

    fun getUidFlow() = dataStore.data
        .catch { exception -> emit(emptyPreferences()) }
        .map { preferences -> preferences[UID] ?: "" }

    suspend fun saveLogin(isLogin: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_LOGIN] = isLogin
        }
    }

    fun isLoginFlow() = dataStore.data.catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[IS_LOGIN] ?: false }

    suspend fun saveUserAccount(qq: String) {
        dataStore.edit { preferences -> preferences[USER_ACCOUNT] = qq }
    }

    fun getUserAccountFlow() = dataStore.data.catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[USER_ACCOUNT] ?: "" }

    suspend fun saveHomeImageUrl(url: String) {
        dataStore.edit { preferences -> preferences[HOME_IMAGE_URL] = url }
    }

    fun getHomeImageUrlFlow() = dataStore.data.catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[HOME_IMAGE_URL] ?: "" }

    suspend fun saveNotificationId(id: Int) {
        dataStore.edit { preferences -> preferences[NOTIFICATION_ID] = id }
    }

    fun getNotificationIdFlow() = dataStore.data.catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[NOTIFICATION_ID] }

    suspend fun saveExp(exp: Int?) {
        dataStore.edit { preferences -> preferences[EXP] = exp ?: 0 }
    }
    fun getExpFlow() = dataStore.data.catch { emit(emptyPreferences()) }.map { preferences -> preferences[EXP] }
}