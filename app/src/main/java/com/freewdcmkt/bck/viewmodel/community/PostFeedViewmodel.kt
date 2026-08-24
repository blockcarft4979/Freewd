package com.freewdcmkt.bck.viewmodel.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.UploadImgData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PostFeedViewmodel : ViewModel() {

    private val _postFeedUiState = MutableStateFlow<PostFeedUiState>(PostFeedUiState.NoAction)
    val postFeedUiState: StateFlow<PostFeedUiState> = _postFeedUiState.asStateFlow()
    fun postFeed(
        zone: Int,
        message: String? = null,
        title: String? = null,
        imgUrl: String? = null
    ) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    _postFeedUiState.value = PostFeedUiState.Upload
                    val feedData: JsonObject = buildJsonObject {
                        put("zone", zone)
                        put("message", message)
                        put("title", title)
                        put("img",imgUrl)
                    }
                    val body = feedData.toString().toRequestBody("application/json".toMediaType())
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.POST_FEED_URL).post(body).build()
                    ).execute()
                }
                val body = response.body.string()
                if (response.isSuccessful) {
                    _postFeedUiState.value = PostFeedUiState.Success
                } else {
                    Log.d("POST FEED VIEWMODEL", body + response.code)
                    _postFeedUiState.value =
                        PostFeedUiState.Error(JsonParser.json.decodeFromString<ErrorData>(body).msg)
                }
            } catch (e: Exception) {
                _postFeedUiState.value = PostFeedUiState.Error(isNoNetwork = true)
            }
        }
    }

    fun uploadImg(img: File) {
        _postFeedUiState.value = PostFeedUiState.Upload
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val requestBody =
                        MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(
                            "file",
                            img.name,
                            img.asRequestBody("image/jpeg".toMediaType())
                        ).build()
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.IMG_UPLOAD_URL).post(requestBody)
                            .build()
                    ).execute()
                }
                val body = response.body.string()
                Log.d("POST FEED",body)
                val data = JsonParser.json.decodeFromString<BaseData<UploadImgData>>(body)
                if (response.isSuccessful && data.data != null) {
                    _postFeedUiState.value = PostFeedUiState.ImageUploaded(data.data.url)
                } else {
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    _postFeedUiState.value = PostFeedUiState.Error(errorData.msg)
                }
            } catch (e: Exception) {
                _postFeedUiState.value = PostFeedUiState.Error(isNoNetwork = true)
            }
        }
    }


}

sealed class PostFeedUiState {
    object NoAction : PostFeedUiState()
    object Upload : PostFeedUiState()
    object Success : PostFeedUiState()
    class Error(val msg: String? = null, val isNoNetwork: Boolean = false) : PostFeedUiState()
    class ImageUploaded(val url: String) : PostFeedUiState()

}