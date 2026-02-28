package com.attendance.app.utils

import androidx.compose.ui.graphics.Color

/**
 * 应用颜色常量
 * 集中管理应用中使用的颜色，便于维护和统一风格
 */
object AppColors {
    // 考勤状态颜色
    val FullDay = Color(0xFF4CAF50)   // 全勤绿色
    val HalfDay = Color(0xFFFFC107)   // 半天黄色

    // 排名颜色
    val RankGold = Color(0xFFFFD700)   // 金色
    val RankSilver = Color(0xFFC0C0C0) // 银色
    val RankBronze = Color(0xFFCD7F32) // 铜色
}
