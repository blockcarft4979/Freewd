package com.freewdcmkt.bck.api

object RetroApi {
    const val BASE_URL = "https://app.freewd.top/api/v1/"

    const val COMMUNITY_BASE_URL = "https://community.freewd.top/app/api/v1/"

    object Auth {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val VERIFY_TOKEN = "auth/verify_token"
        const val SEND_AUTH_CODE = "auth/send_auth_code"
    }

    object Community {
        const val GET_FEED = "community/get_feed"
        const val GET_FEED_DETAIL = "community/get_feed_detail"
        const val LIKE_FEED = "community/like_feed"
        const val REPLY_FEED = "community/reply_feed"
        const val DELETE_FEED = "community/delete_feed"
        const val UPLOAD = "community/upload"
        const val IMG_UPLOAD = "community/img_upload"
    }

    object User {
        const val GET_USER_INFO = "user/get_user_info"
        const val CHECK_IN = "user/check_in"
        const val SUBMIT_USERNAME = "user/submit_username"
    }

    object Other {
        const val HOME_DATA = "community/home_data.json"
        const val FEED_ERROR_HINT = "community/feed_error_hint.json"
    }

    object Document {
        const val PRIVACY_POLICY = "document/privacy_policy.txt"
        const val USER_AGREEMENT = "document/user_agreement.txt"
    }

}