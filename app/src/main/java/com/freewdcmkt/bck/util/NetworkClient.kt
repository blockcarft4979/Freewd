package com.freewdcmkt.bck.util

import okhttp3.OkHttpClient

object NetworkClient {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }
}