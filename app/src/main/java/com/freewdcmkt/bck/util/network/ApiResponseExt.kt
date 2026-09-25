package com.freewdcmkt.bck.util.network

import android.util.Log
import com.freewdcmkt.bck.data.ApiResponse
import com.freewdcmkt.bck.util.JsonParser
import kotlinx.serialization.Serializable
import retrofit2.Response

@Serializable
data class ApiErrorResponse(
    val success: Boolean = false,
    val message: String? = null
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T?) : ApiResult<T>()
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

fun <T> Response<ApiResponse<T>>.toResult(): ApiResult<T> {
    return try {
        if (isSuccessful) {
            val body = body()
            if (body?.isSuccess == true) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body?.errorMessage ?: "未知错误", code())
            }
        } else {
            val errorBodyStr = errorBody()?.string()
            val errorData = try {
                errorBodyStr?.takeIf { it.isNotBlank() }?.let {
                    JsonParser.json.decodeFromString<ApiErrorResponse>(it)  // ← 用非泛型类
                }
            } catch (e: Exception) {
                null
            }
            ApiResult.Error(
                errorData?.message ?: "请求失败(${code()})",
                code()
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

}
suspend fun <T> safeApiCall(
    call: suspend () -> Response<ApiResponse<T>>
): ApiResult<T> {
    return try {
        call().toResult()
    } catch (e: Exception) {
        ApiResult.NetworkError
    }
}