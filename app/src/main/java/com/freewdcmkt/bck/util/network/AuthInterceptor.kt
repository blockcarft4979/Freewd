package com.freewdcmkt.bck.util.network

import com.freewdcmkt.bck.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenManager.getToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        return chain.proceed(newRequest)
    }
}