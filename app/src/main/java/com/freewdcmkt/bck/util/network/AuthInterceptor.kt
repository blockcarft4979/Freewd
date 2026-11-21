package com.freewdcmkt.bck.util.network

import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response

class AuthInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = TokenManager.getToken()
        val request = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)
        val isLoginRequest = originalRequest.url.encodedPath.contains("/login")

        if (response.code == 401 && !isLoginRequest) {

            synchronized(this) {
                runBlocking {
                    TokenManager.clearToken()
                    UserInfoManager.clearAllData()
                }
            }

            return response
        }

        return response
    }
}