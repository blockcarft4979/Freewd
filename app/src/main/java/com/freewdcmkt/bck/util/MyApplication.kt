package com.freewdcmkt.bck.util

import android.app.Application
import com.freewdcmkt.bck.data.common.UserInfoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MyApplication : Application(){
    override fun onCreate(){
        super.onCreate()
        TokenManager.init(this)
        UserInfoManager.init(this)
    }
}