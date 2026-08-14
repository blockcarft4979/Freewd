package com.freewdcmkt.bck.util.network

import com.freewdcmkt.bck.util.network.AuthInterceptor
import okhttp3.OkHttpClient

object NetworkClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }
}