package com.freewdcmkt.bck.util


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsKeys {
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
}

// 读取：默认开启动态取色
fun Context.dynamicColorFlow() = dataStore.data.map { prefs ->
    prefs[SettingsKeys.DYNAMIC_COLOR] ?: true
}

// 写入
suspend fun Context.setDynamicColor(enabled: Boolean) {
    dataStore.edit { prefs ->
        prefs[SettingsKeys.DYNAMIC_COLOR] = enabled
    }
}