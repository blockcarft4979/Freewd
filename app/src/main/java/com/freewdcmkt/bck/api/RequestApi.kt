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


    object Notification {
        const val NOTIFICATION_URL = "${NOTIFICATION_BASE}get_notification"
        const val CLEAR_ALL_NOTIFICATIONS = "${NOTIFICATION_BASE}clear_notification"
    }


    object Document {
        const val PRIVACY_POLICY = "${OTHER_BASE}document/privacy_policy.txt"
        const val USER_AGREEMENT = "${OTHER_BASE}document/user_agreement.txt"
    }
}


fun userAvatarUrl(qq: String): String = "https://q.qlogo.cn/headimg_dl?dst_uin=$qq&spec=640"