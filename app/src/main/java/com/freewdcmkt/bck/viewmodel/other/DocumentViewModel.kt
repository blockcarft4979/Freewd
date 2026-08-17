package com.freewdcmkt.bck.viewmodel.other

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.util.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class DocumentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DocumentUiState>(DocumentUiState.Loading)
    val uiState: StateFlow<DocumentUiState> = _uiState.asStateFlow()
    private val _documentContent = MutableStateFlow("")
    val documentContent: StateFlow<String> = _documentContent.asStateFlow()
    fun fetchData(url: String) {
        _uiState.value = DocumentUiState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder
                            ().url(url).build()
                    ).execute()
                }
                val body = response.body.string()
                Log.d("DOCUMENT VIEWMODEL","$body ${response.code}")
                if (response.isSuccessful) {
                    _documentContent.value = body
                    _uiState.value = DocumentUiState.Finish
                }else{
                    _uiState.value = DocumentUiState.LoadFailed
                }
            } catch (e: Exception) {
                Log.d("DOCUMENT VIEWMODEL",e.message.toString())
                _uiState.value = DocumentUiState.LoadFailed
            }
        }
    }

}

sealed class DocumentUiState() {
    object Loading : DocumentUiState()
    object Finish : DocumentUiState()
    object LoadFailed : DocumentUiState()
}