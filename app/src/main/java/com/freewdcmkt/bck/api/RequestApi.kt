package com.freewdcmkt.bck.api

object RequestApi {

    private const val WEB_BASE = "https://community.freewd.top/"

    object DocumentsWeb{
        const val AGREEMENT = "${WEB_BASE}documents/pages/agreement"
    }
}


fun userAvatarUrl(qq: String): String = "https://q.qlogo.cn/headimg_dl?dst_uin=$qq&spec=640"