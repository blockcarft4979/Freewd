package com.freewdcmkt.bck.api

object RetroApi {
    const val BASE_URL = "https://app.freewd.top/api/v1/"

    const val COMMUNITY_BASE_URL = "https://community.freewd.top/app/api/v1/"

    object Auth {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val VERIFY_TOKEN = "auth/verify_token"
    }
    object Community{
        const val GET_FEED = "community/get_feed"
        const val GET_FEED_DETAIL = "community/get_feed_detail"
        const val LIKE_FEED = "community/like_feed"
    }


    object Other {
        const val HOME_DATA = "community/home_data.json"
    }

    object Document {
        const val PRIVACY_POLICY = "document/privacy_policy.txt"
        const val USER_AGREEMENT = "document/user_agreement.txt"
    }

}