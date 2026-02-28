package com.attendance.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.attendance.app.data.local.dao.AttendanceDao
import com.attendance.app.data.local.dao.WorkerDao
import com.attendance.app.data.local.entity.Attendance
import com.attendance.app.data.local.entity.Worker

/**
 * Room 数据库
 */
@Database(
    entities = [Worker::class, Attendance::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun attendanceDao(): AttendanceDao
}
