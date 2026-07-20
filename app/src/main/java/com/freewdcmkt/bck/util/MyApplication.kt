package com.freewdcmkt.bck.util

import android.app.Application

class MyApplication : Application(){
    override fun onCreate(){
        super.onCreate()
        TokenManager.init(this)
        UserInfoManager.init(this)
    }
}