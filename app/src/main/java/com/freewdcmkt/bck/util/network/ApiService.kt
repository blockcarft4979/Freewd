package com.freewdcmkt.bck.util.network

import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.api.RetroApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.request.LoginRequestData
import com.freewdcmkt.bck.data.request.RegisterRequestData
import com.freewdcmkt.bck.data.screen.LoginData
import com.freewdcmkt.bck.util.JsonParser
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(RetroApi.Auth.LOGIN)
    suspend fun login(@Body request: LoginRequestData): BaseData<LoginData>
    @POST(RetroApi.Auth.REGISTER)
    suspend fun register(@Body request: RegisterRequestData): BaseData<LoginData>
}

object RetroClient {
    private val okHttpClient = NetworkClient.client
    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl(RetroApi.BASE_URL)
        .addConverterFactory(JsonParser.json.asConverterFactory("application/json".toMediaType()))
        .build()
    val apiService : ApiService by lazy { retrofit.create(ApiService::class.java) }
}