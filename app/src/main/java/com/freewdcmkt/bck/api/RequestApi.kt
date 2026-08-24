package com.freewdcmkt.bck.api

/**
 * 所有 API 路由的统一入口
 */
object RequestApi {
    const val BASE_URL = "https://app.freewd.top/api/v1/"
    private const val COMMUNITY_BASE = "${BASE_URL}community/"
    private const val NOTIFICATION_BASE = "${BASE_URL}notification/"
    private const val USER_BASE = "${BASE_URL}user/"
    private const val OTHER_BASE = "https://community.freewd.top/app/api/v1/"

    object Auth {
        const val LOGIN_URL = "${BASE_URL}auth/login"
        const val SEND_AUTH_CODE_URL = "${BASE_URL}auth/send_auth_code"
        const val REGISTER_URL = "${BASE_URL}auth/register"
        const val VERIFY_TOKEN_URL = "${BASE_URL}auth/verify_token"
    }

    object Community {
        private const val FEED_PATH = "${COMMUNITY_BASE}get_feed"
        private const val FEED_DETAIL = "${COMMUNITY_BASE}get_feed_detail"
        const val LIKE_FEED_URL = "${COMMUNITY_BASE}like_feed"
        const val POST_FEED_URL = "${COMMUNITY_BASE}upload"
        private const val DELETE_FEED_URL = "${COMMUNITY_BASE}delete_feed"
        const val REPLY_FEED_URL = "${COMMUNITY_BASE}reply_feed"
        const val IMG_UPLOAD_URL = "${COMMUNITY_BASE}img_upload"
        fun feed(zone: Int, page: Int = 1) = "$FEED_PATH?page=$page&zone=$zone"
        fun feedDetail(id: Int) = "$FEED_DETAIL?id=$id"
        fun deleteFeed(id: Int) = "$DELETE_FEED_URL?id=$id"
    }

    object Notification {
        const val NOTIFICATION_URL = "${NOTIFICATION_BASE}get_notification"
        const val CLEAR_ALL_NOTIFICATIONS = "${NOTIFICATION_BASE}clear_notification"
    }

    object User {
        const val GET_USER_INFO_URL = "${USER_BASE}get_user_info"
        const val SUBMIT_USER_NAME_URL = "${USER_BASE}submit_username"
    }

    object Other {
        const val HOME_DATA = "${OTHER_BASE}community/home_data.json"
    }
    object Document {
        const val PRIVACY_POLICY = "${OTHER_BASE}document/privacy_policy.txt"
        const val USER_AGREEMENT = "${OTHER_BASE}document/user_agreement.txt"
    }
}


fun userAvatarUrl(qq: String): String = "https://q.qlogo.cn/headimg_dl?dst_uin=$qq&spec=640"