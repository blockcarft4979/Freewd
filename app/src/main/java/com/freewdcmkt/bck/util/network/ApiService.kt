package com.freewdcmkt.bck.util.network

import com.freewdcmkt.bck.api.RetroApi
import com.freewdcmkt.bck.data.ApiResponse
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.request.LoginRequestData
import com.freewdcmkt.bck.data.request.RegisterRequestData
import com.freewdcmkt.bck.data.request.SendAuthCodeRequestData
import com.freewdcmkt.bck.data.screen.ChangePasswordRequestData
import com.freewdcmkt.bck.data.screen.CheckInData
import com.freewdcmkt.bck.data.screen.FeedData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.data.screen.LikeFeedRequestData
import com.freewdcmkt.bck.data.screen.LikeFeedResultData
import com.freewdcmkt.bck.data.screen.LikeResult
import com.freewdcmkt.bck.data.screen.LoginData
import com.freewdcmkt.bck.data.screen.NotificationDataList
import com.freewdcmkt.bck.data.screen.PostFeedData
import com.freewdcmkt.bck.data.screen.PostFeedRequestData
import com.freewdcmkt.bck.data.screen.ReplyFeedData
import com.freewdcmkt.bck.data.screen.SearchUsersData
import com.freewdcmkt.bck.data.screen.SubmitPasswordRequestData
import com.freewdcmkt.bck.data.screen.UploadImgData
import com.freewdcmkt.bck.data.screen.UserProfileData
import com.freewdcmkt.bck.data.screen.UsernameData
import com.freewdcmkt.bck.data.screen.VerifyTokenData
import com.freewdcmkt.bck.util.JsonParser
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 仅剩的 v1 接口。
 *
 * v2 迁移后这里只剩 community 那一组（静态 JSON / 旧的 community 站点），
 * 它们由 [CommunityClient] 使用，注意不是 app.freewd.top 的 /api/v1。
 */
interface ApiService {

    @GET(RetroApi.Community.GET_FEED)
    suspend fun getFeed(
        @Query("page") page: Int
    ): Response<BaseData<FeedData>>

    @GET(RetroApi.Community.GET_FEED_DETAIL)
    suspend fun getFeedDetail(@Query("id") id: Int): Response<BaseData<FeedDetailData>>

    @POST(RetroApi.Community.UPLOAD)
    suspend fun upload(@Body requestData: PostFeedRequestData): Response<BaseData<PostFeedData>>

    @Multipart
    @POST(RetroApi.Community.IMG_UPLOAD)
    suspend fun uploadImg(@Part file: MultipartBody.Part): Response<BaseData<UploadImgData>>

    @DELETE(RetroApi.Community.DELETE_FEED)
    suspend fun deleteFeed(@Query("id") id: Int): Response<BaseData<Unit>>

    @POST(RetroApi.Community.REPLY_FEED)
    suspend fun replyFeed(@Body replyData: ReplyFeedData): Response<BaseData<Unit>>

    @POST(RetroApi.Community.LIKE_FEED)
    suspend fun likeFeed(@Body request: LikeFeedRequestData): Response<BaseData<LikeFeedResultData>>

    @GET(RetroApi.Other.HOME_DATA)
    suspend fun getHomeData(): Response<BaseData<HomeData>>

    @GET(RetroApi.Other.FEED_ERROR_HINT)
    suspend fun getFeedErrorHint(): Response<BaseData<FeedDetailData>>

}

/**
 * v2 接口全集（baseUrl = [RetroApi.V2_BASE_URL]，响应统一为 {success, data, message}）
 */
interface V2ApiService {
    // ===== 认证 =====
    @POST(RetroApi.Auth.LOGIN)
    suspend fun login(@Body request: LoginRequestData): Response<ApiResponse<LoginData>>

    @POST(RetroApi.Auth.SEND_AUTH_CODE)
    suspend fun sendAuthCode(@Body requestData: SendAuthCodeRequestData): Response<ApiResponse<Unit>>

    @POST(RetroApi.Auth.REGISTER)
    suspend fun register(@Body request: RegisterRequestData): Response<ApiResponse<LoginData>>

    @POST(value = RetroApi.Auth.SEND_RESET_CODE)
    suspend fun sendResetCode(@Body request: ChangePasswordRequestData): Response<ApiResponse<Unit>>

    @POST(value = RetroApi.Auth.RESET_PASSWORD)
    suspend fun submitPassword(@Body request: SubmitPasswordRequestData): Response<ApiResponse<Unit>>

    @GET(RetroApi.Auth.VERIFY_TOKEN)
    suspend fun verifyToken(): Response<ApiResponse<VerifyTokenData>>

    // ===== 用户 =====
    // 注意是 POST：v2 routes.php 里定义的是 'POST /check_in'（签到会写库，不是幂等查询）
    @POST(RetroApi.User.CHECK_IN)
    suspend fun checkIn(): Response<ApiResponse<CheckInData>>

    /** GET /users/{qq} —— 用户资料（用户主页） */
    @GET("users/{qq}")
    suspend fun getUserProfile(@Path("qq") qq: String): Response<ApiResponse<UserProfileData>>

    @POST(RetroApi.User.SUBMIT_USERNAME)
    suspend fun submitUsername(@Body request: UsernameData): Response<ApiResponse<UsernameData>>

    // ===== 通知 =====
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("mark_read") markRead: Boolean = true
    ): Response<ApiResponse<NotificationDataList>>

    @POST("notifications/clear")
    suspend fun clearNotifications(@Body request: okhttp3.RequestBody): Response<ApiResponse<Unit>>

    // ===== 帖子 =====
    @GET(value = RetroApi.Post.GET_POSTS)
    suspend fun getPosts(@Query("page") id: Int): Response<ApiResponse<FeedData>>

    @GET(value = RetroApi.Post.GET_POST_DETAILS)
    suspend fun getPostDetails(@Path("id") id: Int): Response<ApiResponse<FeedDetailData>>

    @POST(RetroApi.Post.LIKE_POST)
    suspend fun likePost(@Path("id") id: Int): Response<ApiResponse<LikeResult>>

    @DELETE(RetroApi.Post.DELETE_POST)
    suspend fun deletePost(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @POST(RetroApi.Post.REPLY_POST)
    suspend fun replyPost(@Path("id") id: Int, @Body request: ReplyFeedData): Response<ApiResponse<Unit>>

    @POST(RetroApi.Post.UPLOAD_POST)
    suspend fun uploadPost(@Body requestData: PostFeedRequestData): Response<ApiResponse<PostFeedData>>

    @Multipart
    @POST(RetroApi.Post.IMG_UPLOAD)
    suspend fun uploadImg(@Part file: MultipartBody.Part): Response<ApiResponse<UploadImgData>>

    @DELETE(RetroApi.Post.DELETE_REPLY)
    suspend fun deleteReply(@Path("id") id: Int, @Path("rid") rid: Int): Response<ApiResponse<Unit>>

    // ===== 搜索 =====
    // GET /search?q=&page=&limit=  ->  {total, page, limit, pages, feed[]}
    // 返回结构和 GET /posts 同构，所以直接复用 FeedData
    // GET /search?q=&qq=&uid=&username=&page=&limit=  ->  {total, page, limit, pages, feed[]}
    // q / qq / uid / username 可任意组合；只传 qq（不传 q）即「某用户的全部帖子」，
    // 用户主页就是靠这个拉 TA 的帖子，不用额外的服务端接口。
    @GET("search")
    suspend fun searchPosts(
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("qq") authorQq: String? = null,
        @Query("username") authorUsername: String? = null
    ): Response<ApiResponse<FeedData>>

    // GET /users/search?q=&page=&limit=  ->  {total, page, limit, pages, user_list[]}
    @GET("users/search")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<SearchUsersData>>

}

object RetroClient {
    private val okHttpClient = NetworkClient.client
    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl(RetroApi.BASE_URL)
        .addConverterFactory(JsonParser.json.asConverterFactory("application/json".toMediaType()))
        .build()
    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }

}

object RetroV2Client {
    private val okHttpClient = NetworkClient.client
    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl(RetroApi.V2_BASE_URL)
        .addConverterFactory(JsonParser.json.asConverterFactory("application/json".toMediaType()))
        .build()
    val apiService: V2ApiService by lazy { retrofit.create(V2ApiService::class.java) }

}

object CommunityClient {
    private val okHttpClient = NetworkClient.client
    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl(RetroApi.COMMUNITY_BASE_URL)
        .addConverterFactory(JsonParser.json.asConverterFactory("application/json".toMediaType()))
        .build()
    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
