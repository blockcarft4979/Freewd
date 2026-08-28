package com.freewdcmkt.bck.util.network

import android.content.Context
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response

//class AuthInterceptor() : Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val originalRequest = chain.request()
//        val token = TokenManager.getToken()
//        val response = chain.proceed(originalRequest)
//
//        val newRequest = if (token != null) {
//            originalRequest.newBuilder()
//                .header("Authorization", "Bearer $token")
//                .build()
//        } else {
//            originalRequest
//        }
//        if (response.code == 401) {
//            response.close()
//            synchronized(this) {
//                TokenManager.clearToken()
//                runBlocking {
//                    UserInfoManager.clearAllData()
//                }
//
//            }
//        }
//        return response
//    }
//}
class AuthInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. 构建带 Token 的请求（先获取 Token）
        val token = TokenManager.getToken()
        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        if (response.code == 401) {
            response.close()

            synchronized(this) {
                TokenManager.clearToken()
                runBlocking {
                    UserInfoManager.clearAllData()
                }
            }

            return Response.Builder()
                .code(401)
                .message("Unauthorized")
                .protocol(Protocol.HTTP_1_1)
                .request(originalRequest)
                .build()
        }

        return response
    }
}