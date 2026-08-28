package com.freewdcmkt.bck.util.network

import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.util.JsonParser
import retrofit2.HttpException

fun HttpException.getErrorMsg(): String? {
    return try {
        // 注意：errorBody().string() 只能调用一次！
        val body = this.response()?.errorBody()?.string()
        if (body.isNullOrEmpty()) return null
        // 用你已有的 JsonParser 或 Json
        val base = JsonParser.json.decodeFromString<BaseData<Any>>(body)
        base.msg
    } catch (e: Exception) {
        null // 解析失败就返回 null
    }
}