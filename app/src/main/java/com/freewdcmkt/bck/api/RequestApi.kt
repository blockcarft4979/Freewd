package com.freewdcmkt.bck.api

/**
 * 所有 API 路由的统一入口
 */
object RequestApi {
    private const val BASE_URL = "https://app.freewd.top/app/api/v1"
    private const val COMMUNITY_BASE = "$BASE_URL/community"
    private const val OTHER_BASE = "https://community.freewd.top/app/api/v1"

    object Auth {
        const val LOGIN_URL = "$BASE_URL/auth/login.php"
    }

    object Community {
        private const val FEED_PATH = "$COMMUNITY_BASE/get_feed.php"
        private const val FEED_DETAIL = "$COMMUNITY_BASE/get_feed_detail.php"
        fun feed(zone: Int, page: Int = 1) = "$FEED_PATH?page=$page&zone=$zone"
        fun feedDetail(id: String) = "$FEED_DETAIL?id=$id"
    }

    object Other {
        const val HOME_DATA = "$OTHER_BASE/community/home_data.json"
    }
}


fun userAvatarUrl(qq: String): String = "https://q.qlogo.cn/headimg_dl?dst_uin=$qq&spec=640"