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
        private const val LIKE_FEED_URL  = "$COMMUNITY_BASE/like_feed.php"
         const val POST_FEED_URL = "$COMMUNITY_BASE/upload.php"

        fun feed(zone: Int, page: Int = 1) = "$FEED_PATH?page=$page&zone=$zone"
        fun feedDetail(id: Int,zone: Int) = "$FEED_DETAIL?id=$id&zone=$zone"
        fun likeFeed(id: Int,zone: Int) = "$LIKE_FEED_URL?id=$id&zone=$zone"
    }

    object Other {
        const val HOME_DATA = "$OTHER_BASE/community/home_data.json"
    }
}


fun userAvatarUrl(qq: String): String = "https://q.qlogo.cn/headimg_dl?dst_uin=$qq&spec=640"