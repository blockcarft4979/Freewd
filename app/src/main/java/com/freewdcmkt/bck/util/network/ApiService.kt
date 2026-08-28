package com.freewdcmkt.bck.util.network

import com.freewdcmkt.bck.api.RetroApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.request.LikeFeedRequestData
import com.freewdcmkt.bck.data.request.LoginRequestData
import com.freewdcmkt.bck.data.request.RegisterRequestData
import com.freewdcmkt.bck.data.screen.CheckInData
import com.freewdcmkt.bck.data.screen.FeedData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.data.screen.LikeFeedResultData
import com.freewdcmkt.bck.data.screen.LoginData
import com.freewdcmkt.bck.data.screen.UserData
import com.freewdcmkt.bck.data.screen.UsernameData
import com.freewdcmkt.bck.data.screen.VerifyTokenData
import com.freewdcmkt.bck.util.JsonParser
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST(RetroApi.Auth.LOGIN)
    suspend fun login(@Body request: LoginRequestData): Response<BaseData<LoginData>>

    @POST(RetroApi.Auth.REGISTER)
    suspend fun register(@Body request: RegisterRequestData): Response<BaseData<LoginData>>

    @GET(RetroApi.Auth.VERIFY_TOKEN)
    suspend fun verifyToken(): Response<BaseData<VerifyTokenData>>

    @GET(RetroApi.Community.GET_FEED)
    suspend fun getFeed(
        @Query("zone") zone: Int,
        @Query("page") page: Int
    ): Response<BaseData<FeedData>>

    @GET(RetroApi.Community.GET_FEED_DETAIL)
    suspend fun getFeedDetail(@Query("id") id: Int): Response<BaseData<FeedDetailData>>

    @POST(RetroApi.Community.LIKE_FEED)
    suspend fun replyFeed(@Body request: LikeFeedRequestData): Response<BaseData<LikeFeedResultData>>


    @GET(RetroApi.User.CHECK_IN)
    suspend fun checkIn(): Response<BaseData<CheckInData>>

    @POST(RetroApi.User.SUBMIT_USERNAME)
    suspend fun submitUsername(@Body request: UsernameData): Response< BaseData<UsernameData>>

    @GET(RetroApi.Other.HOME_DATA)
    suspend fun getHomeData(): Response<BaseData<HomeData>>

}

object RetroClient {
    private val okHttpClient = NetworkClient.client
    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl(RetroApi.BASE_URL)
        .addConverterFactory(JsonParser.json.asConverterFactory("application/json".toMediaType()))
        .build()
    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

}

object CommunityClient {
    private val okHttpClient = NetworkClient.client
    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl(RetroApi.COMMUNITY_BASE_URL)
        .addConverterFactory(JsonParser.json.asConverterFactory("application/json".toMediaType()))
        .build()
    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}