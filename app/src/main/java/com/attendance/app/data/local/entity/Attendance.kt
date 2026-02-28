package com.attendance.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 考勤记录实体类
 */
@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workerId", "date"], unique = true)]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workerId: Long,
    val date: String,           // 格式: yyyy-MM-dd
    val morning: Boolean,       // 上午是否上班
    val afternoon: Boolean,     // 下午是否上班
    val overtimeHours: Double,  // 加班小时数
    val remark: String = ""     // 备注
)
