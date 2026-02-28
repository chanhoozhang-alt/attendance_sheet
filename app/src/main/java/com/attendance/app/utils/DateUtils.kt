package com.attendance.app.utils

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * 日期工具类
 * 使用 java.time API（API 26+），线程安全
 */
object DateUtils {

    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_FORMAT_DISPLAY = "yyyy年MM月dd日"
    const val MONTH_FORMAT = "yyyy-MM"

    /**
     * 格式化日期
     */
    fun formatDate(year: Int, month: Int, day: Int): String {
        val date = LocalDate.of(year, month + 1, day)
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
    }

    /**
     * 将 yyyy-MM-dd 格式转换为显示格式
     */
    fun formatDisplayDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT))
            date.format(DateTimeFormatter.ofPattern(DATE_FORMAT_DISPLAY, Locale.CHINA))
        } catch (e: DateTimeParseException) {
            dateString
        }
    }

    /**
     * 获取当前日期 yyyy-MM-dd
     */
    fun getCurrentDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT))
    }

    /**
     * 获取当前月份 yyyy-MM
     */
    fun getCurrentMonth(): String {
        return YearMonth.now().format(DateTimeFormatter.ofPattern(MONTH_FORMAT))
    }

    /**
     * 获取月份起始日期
     */
    fun getMonthStart(month: String): String {
        return try {
            val yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT))
            "${month}-01"
        } catch (e: Exception) {
            "${getCurrentMonth()}-01"
        }
    }

    /**
     * 获取月份结束日期
     */
    fun getMonthEnd(month: String): String {
        return try {
            val yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern(MONTH_FORMAT))
            val lastDay = yearMonth.lengthOfMonth()
            "${month}-${lastDay.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            val yearMonth = YearMonth.now()
            "${yearMonth.format(DateTimeFormatter.ofPattern(MONTH_FORMAT))}-${yearMonth.lengthOfMonth()}"
        }
    }

    /**
     * 获取某月的天数
     * @param year 年份
     * @param month 月份（0-11，Calendar 兼容）
     */
    fun getDaysInMonth(year: Int, month: Int): Int {
        return try {
            val yearMonth = YearMonth.of(year, month + 1)
            yearMonth.lengthOfMonth()
        } catch (e: Exception) {
            30
        }
    }

    /**
     * 解析日期字符串
     * @return Triple<year, month, day> month 是 0-indexed（兼容 Calendar）
     */
    fun parseDate(dateString: String): Triple<Int, Int, Int> {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DATE_FORMAT))
            Triple(date.year, date.monthValue - 1, date.dayOfMonth)
        } catch (e: DateTimeParseException) {
            val now = LocalDate.now()
            Triple(now.year, now.monthValue - 1, now.dayOfMonth)
        }
    }
}
