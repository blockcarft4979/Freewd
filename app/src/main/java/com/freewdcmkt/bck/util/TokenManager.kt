package com.freewdcmkt.bck.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// preferencesDataStore 委托必须在顶层声明，且每个文件名只允许有一个委托
private val Context.tokenDataStore by preferencesDataStore(name = "token")

object TokenManager {
    private val TOKEN = stringPreferencesKey("token")

    private lateinit var dataStore: DataStore<Preferences>
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 内存缓存。@Volatile 保证拦截器线程能看到 init 线程的写入 */
    @Volatile
    private var cachedToken: String? = null

    /** 首次加载完成信号，避免 init 阻塞主线程，又保证 getToken 能等到值 */
    private var loadSignal = CompletableDeferred<Unit>()

    fun init(context: Context) {
        dataStore = context.tokenDataStore
        loadSignal = CompletableDeferred()
        scope.launch {
            cachedToken = runCatching {
                dataStore.data
                    .catch { emit(emptyPreferences()) }
                    .map { it[TOKEN] }
                    .first()
            }.getOrNull()
            loadSignal.complete(Unit)
        }
    }

    /**
     * 同步读取，供 OkHttp 拦截器等非协程场景使用。
     * 若首次加载尚未完成会阻塞等待——拦截器运行在 OkHttp 的 IO 线程，可接受。
     * 不要在主线程调用（首次加载可能会阻塞几十毫秒）。
     */
    fun getToken(): String? {
        if (!loadSignal.isCompleted) {
            runBlocking { loadSignal.await() }
        }
        return cachedToken
    }
    // TokenManager.kt 追加
    fun getTokenFlow(): Flow<String?> =
        dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[TOKEN] }
    suspend fun saveToken(token: String) {
        cachedToken = token
        dataStore.edit { it[TOKEN] = token }
    }

    suspend fun clearToken() {
        cachedToken = null
        dataStore.edit { it.remove(TOKEN) }
    }

}