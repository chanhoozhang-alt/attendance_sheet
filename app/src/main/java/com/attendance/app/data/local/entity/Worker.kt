package com.attendance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 工人实体类
 */
@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dailyWage: Double,           // 日工资
    val overtimeHourlyWage: Double,  // 加班时薪
    val remark: String = ""          // 备注
)
