package com.freewdcmkt.bck.api

object RetroApi {
    const val BASE_URL = "https://app.freewd.top/app/api/v1/"
    const val COMMUNITY_BASE_URL = "https://community.freewd.top/app/api/v1/"

    object Auth {
        const val LOGIN = "auth/login"
        const val REGISTER = "auth/register"
        const val VERIFY_TOKEN = "auth/verify_token"
    }
    object Other{
        const val HOME_DATA = "community/home_data.json"
    }
}