package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class FeedDetailViewmodel : ViewModel() {
    private val _feedDetailUiState = MutableStateFlow<FeedDetailUiState>(FeedDetailUiState.Loading)
    val feedDetailUiState: StateFlow<FeedDetailUiState> = _feedDetailUiState.asStateFlow()
    fun fetchData(id: String) {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.feedDetail(id)).build()
                    ).execute()
                }
                val body = response.body.string()
                if (response.isSuccessful) {
                    val date = JsonParser.json.decodeFromString<BaseData<FeedDetailData>>(body)
                    if (date.data != null) {
                        _feedDetailUiState.value = FeedDetailUiState.Success(date.data)
                    }
                }
            } catch (e: Exception) {
                Log.d("FEED DETAIL VIEWMODEL",e.message.toString())
                _feedDetailUiState.value = FeedDetailUiState.Error
            }
        }

    }
}

sealed class FeedDetailUiState {
    object Loading : FeedDetailUiState()
    class Success(val feedDetailData: FeedDetailData) : FeedDetailUiState()
    object Error : FeedDetailUiState()
}