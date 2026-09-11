package com.freewdcmkt.bck.viewmodel.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.screen.PostFeedRequestData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PostFeedViewmodel : ViewModel() {

    private val _postFeedUiState = MutableStateFlow<PostFeedUiState>(PostFeedUiState.NoAction)
    val postFeedUiState: StateFlow<PostFeedUiState> = _postFeedUiState.asStateFlow()
    fun postFeed(
        zone: Int,
        message: String,
        isAnonymous: Boolean = false,
        title: String? = null,
        imgUrl: String? = null
    ) {
        _postFeedUiState.value = PostFeedUiState.Upload
        viewModelScope.launch {
            try {
                val response =
                    RetroClient.apiService.upload(PostFeedRequestData(zone,isAnonymous, message, title, imgUrl))
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("POST RESULT DATA", data.toString())
                    _postFeedUiState.value = PostFeedUiState.Success
                    if (data?.data?.xp != null) UserInfoManager.saveExp(data.data.xp)
                } else {
                    val errorData = response.errorBody()?.string() ?: ""
                    _postFeedUiState.value =
                        PostFeedUiState.Error(
                            JsonParser.json.decodeFromString<BaseData<Nothing>>(
                                errorData
                            ).msg
                        )
                }
            } catch (e: Exception) {
                Log.d("POST FEED ERROR", e.message.toString())
                _postFeedUiState.value = PostFeedUiState.Error(isNoNetwork = true)
            }
        }
    }

    fun uploadImg(img: File) {
        _postFeedUiState.value = PostFeedUiState.Upload
        viewModelScope.launch {
            try {
                val file = img.asRequestBody("image/jpeg".toMediaType())
                val part = MultipartBody.Part.createFormData("file", img.name, file)
                val response = RetroClient.apiService.uploadImg(part)

                if (response.isSuccessful) {
                    val baseData = response.body()
                    if (baseData?.data != null) {
                        _postFeedUiState.value = PostFeedUiState.ImageUploaded(baseData.data.url)
                    }
                } else {
                    val errorData = response.errorBody()?.string() ?: ""
                    val errorMsg = JsonParser.json.decodeFromString<BaseData<Nothing>>(errorData)
                    _postFeedUiState.value = PostFeedUiState.Error(errorMsg.msg)
                }
            } catch (e: Exception) {
                Log.d("UPLOAD IMG ERROR", e.message.toString())
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