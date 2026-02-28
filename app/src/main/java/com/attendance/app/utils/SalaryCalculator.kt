package com.attendance.app.utils

import com.attendance.app.data.local.entity.Attendance
import com.attendance.app.data.local.entity.Worker

/**
 * 工资计算结果
 */
data class SalaryResult(
    val fullDays: Int,          // 全勤天数
    val halfDays: Int,          // 半天出勤天数
    val overtimeHours: Double,  // 总加班时长
    val totalSalary: Double     // 总工资
)

/**
 * 工资计算工具类
 */
object SalaryCalculator {

    /**
     * 计算工资
     * 规则:
     * - 全天 = 上午 + 下午 = 1天工资
     * - 半天 = 仅上午 或 仅下午 = 0.5天工资
     * - 加班按小时计算
     */
    fun calculateSalary(worker: Worker, records: List<Attendance>): SalaryResult {
        var fullDays = 0
        var halfDays = 0
        var overtime = 0.0

        for (record in records) {
            when {
                record.morning && record.afternoon -> fullDays++
                record.morning || record.afternoon -> halfDays++
            }
            overtime += record.overtimeHours
        }

        val salary = fullDays * worker.dailyWage +
                halfDays * (worker.dailyWage / 2) +
                overtime * worker.overtimeHourlyWage

        return SalaryResult(fullDays, halfDays, overtime, salary)
    }

    /**
     * 计算单个考勤记录的当日工资
     */
    fun calculateDailySalary(
        attendance: Attendance,
        dailyWage: Double,
        overtimeHourlyWage: Double
    ): Double {
        val daySalary = when {
            attendance.morning && attendance.afternoon -> dailyWage
            attendance.morning || attendance.afternoon -> dailyWage / 2
            else -> 0.0
        }
        val overtimeSalary = attendance.overtimeHours * overtimeHourlyWage
        return daySalary + overtimeSalary
    }
}
