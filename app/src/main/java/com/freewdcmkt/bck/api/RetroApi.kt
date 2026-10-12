package com.freewdcmkt.bck.api

object RetroApi {
    const val BASE_URL = "https://app.freewd.top/api/v1/"
    const val V2_BASE_URL = "https://app.freewd.top/api/v2/index.php/"

    const val COMMUNITY_BASE_URL = "https://community.freewd.top/app/api/v1/"

    object Auth {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val VERIFY_TOKEN = "auth/verify"
        const val SEND_AUTH_CODE = "auth/send_auth_code"
        const val SEND_RESET_CODE = "auth/send_reset_code"
        const val RESET_PASSWORD = "auth/reset_password"
    }
    object Post{
        const val GET_POSTS = "posts"
        const val UPLOAD_POST = "posts"
        const val LIKE_POST = "posts/{id}/like"
        const val GET_POST_DETAILS = "posts/{id}"
        const val DELETE_POST = "posts/{id}"
        const val REPLY_POST = "posts/{id}/replies"
        const val DELETE_REPLY = "posts/{id}/replies/{rid}"
        const val IMG_UPLOAD = "uploads"
    }


    /** v2 用户相关接口 */
    object User {
        const val ME = "users/me"
        const val CHECK_IN = "check_in"
        const val SUBMIT_USERNAME = "users/me/username"
    }

    object Other {
        const val HOME_DATA = "community/home_data.json"
        const val FEED_ERROR_HINT = "community/feed_error_hint.json"
    }


}