package com.freewdcmkt.bck.viewmodel.nav

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.file.FilePath
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.data.screen.UsernameData
import com.freewdcmkt.bck.data.screen.VerifyTokenData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.JsonParser.json
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.CommunityClient
import com.freewdcmkt.bck.util.network.NetworkClient
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class HomeViewmodel(application: Application) : AndroidViewModel(application) {
    private val app = application
    val userAccount = UserInfoManager.getUserAccountFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val username = UserInfoManager.getUsernameFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val uid = UserInfoManager.getUidFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val exp = UserInfoManager.getExpFlow().map { it ?: 0 }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()
    private val _uiState = MutableStateFlow<MeUiState>(MeUiState.NoAction)
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()
    private val _homeData = MutableStateFlow(HomeData(null, emptyList()))
    val homeData: StateFlow<HomeData> = _homeData.asStateFlow()
    private val _savedNotificationId = MutableStateFlow(0)
    private val _isShowNotification = MutableStateFlow(false)
    val isShowNotification: StateFlow<Boolean> = _isShowNotification.asStateFlow()
    private val _isShowNoNetwork = MutableStateFlow(false)
    val isShowNoNetwork: StateFlow<Boolean> = _isShowNoNetwork.asStateFlow()


    private val _verifyTokenData = MutableStateFlow(VerifyTokenData("", 0))
    val verifyTokenData: StateFlow<VerifyTokenData> = _verifyTokenData.asStateFlow()

    init {
        viewModelScope.launch {
            UserInfoManager.getNotificationIdFlow().collect { id ->
                if (id != null) {
                    _savedNotificationId.value = id
                }
            }
        }
        viewModelScope.launch {
            val cached = loadHomeDataFromCache()
            if (cached != null) {
                _homeData.value = cached
                _homeUiState.value = HomeUiState.Finish
                Log.d("HOme", "EMPTY")
            } else {
                Log.d("Home", "CACHED DATA")
            }
        }
        fetchData(true)
        verifyToken()
    }

    fun fetchData(forceRefresh: Boolean = false) {
        Log.d("HOME VM", "是否刷新$forceRefresh")
        if (!forceRefresh) {
            return
        }

        viewModelScope.launch {
            try {
                _isShowNoNetwork.value = false
                _homeUiState.value = HomeUiState.Loading
                val response = CommunityClient.apiService.getHomeData()
                if (response.data != null) {
                    if (response.data.notification?.id != _savedNotificationId.value) {
                        _isShowNotification.value = true
                    }
                    _homeUiState.value = HomeUiState.Finish
                    _homeData.value = response.data
                    viewModelScope.launch { saveHomeDataToCache(response.data) }
                } else {
                    _homeUiState.value = HomeUiState.NoNetwork
                }
            } catch (e: Exception) {
                _isShowNoNetwork.value = true
                _homeUiState.value = HomeUiState.Error(null)
                e.printStackTrace()
            }
        }
    }

    fun dismissNotification(id: Int? = null) {
        _isShowNotification.value = false
        if (id != null) {
            _savedNotificationId.value = id
            viewModelScope.launch {
                UserInfoManager.saveNotificationId(id)
            }
        }

    }

    fun submitUsername(username: String) {
        val requestBody = buildJsonObject { put("username", username) }.toString()
            .toRequestBody("application/json".toMediaType())
        viewModelScope.launch {
            _uiState.value = MeUiState.SubmittingUsername
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.User.SUBMIT_USER_NAME_URL)
                            .post(requestBody)
                            .build()
                    ).execute()
                }
                val body = response.body.string()
                val data = json.decodeFromString<BaseData<UsernameData>>(body)

                if (response.isSuccessful && data.data != null) {

                    _uiState.value = MeUiState.SubmitFinish
                    UserInfoManager.saveUsername(data.data.username)
                } else {
                    val errorData = json.decodeFromString<ErrorData>(body)
                    _uiState.value = MeUiState.LoadError(errorData.msg)
                }
            } catch (e: Exception) {
                _isShowNoNetwork.value = true
                _uiState.value = MeUiState.LoadError(isNoNetWork = true)
            }
        }
    }

    fun verifyToken() {
        viewModelScope.launch {
            try {
                val response = RetroClient.apiService.verifyToken()
                if (response.data != null) {
                    UserInfoManager.saveUsername(response.data.username)
                    _verifyTokenData.value = response.data
                } else {
                    TokenManager.clearToken()
                    _homeUiState.value = HomeUiState.Error(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getCacheFile(): File {
        return File(app.filesDir, FilePath.HOME_DATA_CACHE).apply {
            parentFile?.mkdirs()
        }
    }

    private suspend fun saveHomeDataToCache(data: HomeData) {

        withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile()
                val jsonString = json.encodeToString(data)
                file.writeText(jsonString)
                Log.d("HomeVM", "缓存写入成功，路径=${file.absolutePath}, 大小=${file.length()}")

            } catch (e: Exception) {
                Log.e("HomeVM", "保存缓存失败${e.message}", e)
            }
        }
    }

    // 读取缓存（IO 线程），返回 null 表示无缓存或损坏
    private suspend fun loadHomeDataFromCache(): HomeData? {
        return withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile()
                if (!file.exists()) return@withContext null
                val json = file.readText()
                Log.d("HOME DATA", json)
                JsonParser.json.decodeFromString<HomeData>(json)
            } catch (e: Exception) {
                Log.e("HomeVM", "读取缓存失败", e)
                null
            }
        }
    }
}

sealed class HomeUiState {

    object Loading : HomeUiState()
    object NoNetwork : HomeUiState()
    object Finish : HomeUiState()
    class Error(val msg: String? = null) : HomeUiState()

}

sealed class MeUiState() {
    object NoAction : MeUiState()
    object SubmittingUsername : MeUiState()
    object SubmitFinish : MeUiState()

    class LoadError(val msg: String? = null, val isNoNetWork: Boolean = false) : MeUiState()
}