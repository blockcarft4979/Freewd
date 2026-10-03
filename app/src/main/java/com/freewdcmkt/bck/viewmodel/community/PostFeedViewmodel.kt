package com.freewdcmkt.bck.viewmodel.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.screen.PostFeedRequestData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroClient
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
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
        message: String,
        isAnonymous: Boolean = false,
        isMarkdown: Boolean = false,
        title: String? = null,
        imgUrl: String? = null
    ) {
        _postFeedUiState.value = PostFeedUiState.Upload
        val postContent = message.trimEnd()
        if (postContent.isEmpty()) return

        viewModelScope.launch {
            val result = safeApiCall {
                RetroV2Client.apiService.uploadPost(
                    PostFeedRequestData(
                        isAnonymous,
                        isMarkdown,
                        postContent,
                        title,
                        imgUrl
                    )
                )
            }
            when (result) {
                is ApiResult.Success -> {
                    _postFeedUiState.value = PostFeedUiState.Success
                    if (result.data?.xp != null) UserInfoManager.saveExp(result.data.xp)
                }

                is ApiResult.Error -> {
                    _postFeedUiState.value = PostFeedUiState.Error(msg = result.message)
                }

                is ApiResult.NetworkError -> {
                    _postFeedUiState.value = PostFeedUiState.Error(isNoNetwork = true)
                }
            }

        }
    }

    fun uploadImg(img: File) {
        _postFeedUiState.value = PostFeedUiState.Upload
        viewModelScope.launch {

            val file = img.asRequestBody("image/jpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("file", img.name, file)
            val result = safeApiCall { RetroV2Client.apiService.uploadImg(part) }
            when (result) {
                is ApiResult.Success -> {
                    if (result.data?.url != null) _postFeedUiState.value = PostFeedUiState.ImageUploaded(result.data.url) else _postFeedUiState.value =
                        PostFeedUiState.Error()
                }
                is ApiResult.Error -> {
                    _postFeedUiState.value = PostFeedUiState.Error(msg = result.message)
                }

                is ApiResult.NetworkError -> {
                    _postFeedUiState.value = PostFeedUiState.Error(isNoNetwork = true)
                }

            }
        }
    }

    fun resetUi() {
        _postFeedUiState.value = PostFeedUiState.NoAction
    }

}

sealed class PostFeedUiState {
    object NoAction : PostFeedUiState()
    object Upload : PostFeedUiState()
    object Success : PostFeedUiState()
    class Error(val msg: String? = null, val isNoNetwork: Boolean = false) : PostFeedUiState()
    class ImageUploaded(val url: String) : PostFeedUiState()

}