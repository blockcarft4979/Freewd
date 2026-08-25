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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private suspend fun <T> DataStore<Preferences>.setValue(key: Preferences.Key<T>, value: T) {
    edit { preferences -> preferences[key] = value }
}

private fun <T> DataStore<Preferences>.getValueFlow(
    key: Preferences.Key<T>,
    default: T
): Flow<T> = data
    .catch { emit(emptyPreferences()) }
    .map { preferences -> preferences[key] ?: default }

object UserInfoManager {
    private val Context.dataStore by preferencesDataStore(name = "user_info")
    private val USERNAME = stringPreferencesKey("username")
    private val UID = stringPreferencesKey("uid")
    private val IS_LOGIN = booleanPreferencesKey("is_login")
    private val USER_ACCOUNT = stringPreferencesKey("user_account")
    private val HOME_IMAGE_URL = stringPreferencesKey("home_image_url")
    private val NOTIFICATION_ID = intPreferencesKey("notification_id")
    private val EXP = intPreferencesKey("exp")
    private val CHECK_IN_DAYS = intPreferencesKey("check_in_days")
    private val CHECK_IN_DATE = stringPreferencesKey("last_check_in_date")
    private lateinit var dataStore: DataStore<Preferences>
    fun init(context: Context) {
        dataStore = context.dataStore
    }

    suspend fun saveUsername(username: String) = dataStore.setValue(USERNAME, username)

    fun getUsernameFlow() = dataStore.getValueFlow(USERNAME, "")

    suspend fun saveUid(uid: String) = dataStore.setValue(UID, uid)

    fun getUidFlow() = dataStore.getValueFlow(UID, "")
    suspend fun saveLogin(isLogin: Boolean) = dataStore.setValue(IS_LOGIN, isLogin)

    fun isLoginFlow() = dataStore.getValueFlow(IS_LOGIN, false)

    suspend fun saveUserAccount(qq: String) = dataStore.setValue(USER_ACCOUNT, qq)

    fun getUserAccountFlow() = dataStore.getValueFlow(USER_ACCOUNT, "")

    suspend fun saveNotificationId(id: Int) = dataStore.setValue(NOTIFICATION_ID, id)

    fun getNotificationIdFlow() = dataStore.getValueFlow(NOTIFICATION_ID, 0)

    suspend fun saveExp(exp: Int?) = dataStore.setValue(EXP, exp ?: 0)

    fun getExpFlow() = dataStore.getValueFlow(EXP, 0)

    suspend fun saveCheckInDays(day: Int?) = dataStore.setValue(CHECK_IN_DAYS, day ?: 0)
    fun getCheckInDaysFlow() = dataStore.getValueFlow(CHECK_IN_DAYS, 0)

    suspend fun saveLastCheckInDate(date: String) = dataStore.setValue(CHECK_IN_DATE,date)
    fun getLastCheckInDate() = dataStore.getValueFlow(CHECK_IN_DATE,"")
}