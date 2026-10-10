package com.freewdcmkt.bck.api

object RequestApi {

    private const val OTHER_BASE = "https://community.freewd.top/app/api/v1/"

    object Document {
        const val USER_AGREEMENT = "${OTHER_BASE}document/user_agreement.txt"
    }
}


fun userAvatarUrl(qq: String): String = "https://q.qlogo.cn/headimg_dl?dst_uin=$qq&spec=640"