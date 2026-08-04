package com.freewdcmkt.bck.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*


fun formatTime(dateStr: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        val timeInMillis = date.time

        // minResolution = MINUTE_IN_MILLIS 表示最小显示到分钟
        DateUtils.getRelativeTimeSpanString(
            timeInMillis,
            System.currentTimeMillis(),
            DateUtils.SECOND_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    } catch (e: Exception) {
        dateStr // 解析失败时返回原始字符串
    }
}