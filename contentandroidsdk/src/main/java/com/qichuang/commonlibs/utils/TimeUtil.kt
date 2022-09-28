package com.qichuang.commonlibs.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

/**
 * author ：Seven
 * date : 11/30/21
 * description :
 */
class TimeUtil {

    private val second = 1000.toLong()// 1秒钟
    private val minute = 60 * second// 1分钟
    private val hour = 60 * minute // 1小时
    private val day = 24 * hour // 1天
    private val month = 31 * day // 月
    private val year = 12 * month // 年

    /**
     * 返回文字描述的日期
     *
     * @param timestamp
     * @return
     */
    fun getTimeFormatText(timestamp: Long?): String {
        if (timestamp == null) {
            return ""
        }
        val diff: Long = Date().time - timestamp
        var r: Long
        if (diff > year) {
            r = diff / year
            return r.toString() + "年前"
        }
        if (diff > month) {
            r = diff / month
            return r.toString() + "个月前"
        }
        if (diff > day) {
            r = diff / day
            return r.toString() + "天前"
        }
        if (diff > hour) {
            r = diff / hour
            return r.toString() + "个小时前"
        }
        if (diff > minute) {
            r = diff / minute
            return r.toString() + "分钟前"
        }
        return "刚刚"
    }

    /**
     * 返回今天，昨天，更早
     *
     * @param dateStr
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun getDateFormatText(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) {
            return ""
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timestamp = dateFormat.parse(dateStr).time
        val diff: Long = Date().time - timestamp
        val r: Long
        if (diff > day) {
            r = diff / day
            return if (r > 1) {
                "更早"
            } else {
                "昨天"
            }
        }
        return "今天"
    }
}