package com.vkas.secondtranslation.utils

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object CalendarUtils {
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_FORMAT_DETAILED = "yyyy-MM-dd HH:mm:ss"

    private val dateThreadFormat: ThreadLocal<SimpleDateFormat?> = object : ThreadLocal<SimpleDateFormat?>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat(DATE_FORMAT)
        }
    }

    fun dateAfterDate(startTime: String?, endTime: String?): Boolean {
        val format = SimpleDateFormat(DATE_FORMAT)
        try {
            val startDate: Date = format.parse(startTime)
            val endDate: Date = format.parse(endTime)
            val start: Long = startDate.time
            val end: Long = endDate.time
            return end > start
        } catch (e: ParseException) {
            e.printStackTrace()
            return false
        }
    }

    fun formatDateNow(): String? {
        val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)
        return simpleDateFormat.format(Date())
    }

    @Throws(ParseException::class)
    fun dateToStamp(s: String?): String? {
        val simpleDateFormat = SimpleDateFormat(DATE_FORMAT_DETAILED)
        val date: Date = simpleDateFormat.parse(s)
        val ts: Long = date.time
        return ts.toString()
    }

    fun stampToDate(s: String): String? {
        @SuppressLint("SimpleDateFormat")
        val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)
        val date = Date(s.toLong())
        return simpleDateFormat.format(date)
    }

    fun formatDetailedDateNow(isEnd: Boolean): String? {
        val simpleDateFormat = if (isEnd) {
            SimpleDateFormat("$DATE_FORMAT_DETAILED", Locale.getDefault())
        } else {
            SimpleDateFormat(DATE_FORMAT_DETAILED, Locale.getDefault())
        }
        return simpleDateFormat.format(Date())
    }

    fun getTimeToDateFormat1(time: String?): String? {
        var cTime = ""
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日")
        try {
            val date: Date = dateFormat.parseObject(time) as Date
            val simpleDateFormat = SimpleDateFormat(DATE_FORMAT)
            cTime = simpleDateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return cTime
    }

    fun getTimeToDateFormat2(time: String?): String? {
        var cTime = ""
        val dateFormat = SimpleDateFormat(DATE_FORMAT)
        try {
            val date: Date = dateFormat.parseObject(time) as Date
            val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日")
            cTime = simpleDateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return cTime
    }
}