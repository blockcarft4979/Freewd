package com.freewdcmkt.bck.util.time
import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.Direction
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit
import android.icu.text.DisplayContext
import android.icu.util.ULocale
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


fun formatTime(dateStr: String): String {
    return try {

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(dateStr) ?: return dateStr
        val timeInMillis = date.time

        val now = System.currentTimeMillis()
        val diffMillis = now - timeInMillis

        if (diffMillis < 0) {
            return RelativeDateTimeFormatter.getInstance().format(Direction.PLAIN, RelativeDateTimeFormatter.AbsoluteUnit.NOW)
        }

        val diffMinutes = diffMillis / (1000 * 60)
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        // 2. 核心逻辑：一周以内，使用相对时间
        if (diffDays < 7) {
            val formatter = RelativeDateTimeFormatter.getInstance(
                ULocale.getDefault(),
                null,
                RelativeDateTimeFormatter.Style.SHORT,
                DisplayContext.CAPITALIZATION_NONE
            )

            return when {
                diffMinutes < 1 -> formatter.format(Direction.PLAIN, RelativeDateTimeFormatter.AbsoluteUnit.NOW) // 刚刚
                diffMinutes < 60 -> formatter.format(diffMinutes.toDouble(), Direction.LAST, RelativeUnit.MINUTES)
                diffHours < 24 -> formatter.format(diffHours.toDouble(), Direction.LAST, RelativeUnit.HOURS)
                else -> formatter.format(diffDays.toDouble(), Direction.LAST, RelativeUnit.DAYS)
            }
        }

        // 3. 超过一周：显示绝对日期
        // 把报错的两行替换成这样：
        val targetCalendar = Calendar.getInstance()
        targetCalendar.timeInMillis = timeInMillis

        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = now

        val targetYear = targetCalendar.get(Calendar.YEAR)
        val currentYear = currentCalendar.get(Calendar.YEAR)

        val isSameYear = targetYear == currentYear

        val skeleton = if (isSameYear) {
            "MMMdd HH:mm"
        } else {
            "yMMMdd HH:mm"
        }


        val bestPattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton)

        val outputFormatter = SimpleDateFormat(bestPattern, Locale.getDefault())
        outputFormatter.format(date)

    } catch (e: Exception) {
        dateStr
    }
}
fun getSystemDate(): String {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}