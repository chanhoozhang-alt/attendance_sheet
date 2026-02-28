package com.attendance.app

import android.app.Application
import androidx.room.Room
import com.attendance.app.data.local.AppDatabase
import com.attendance.app.data.repository.AttendanceRepository
import com.attendance.app.data.repository.WorkerRepository

/**
 * Application 类，负责初始化数据库和仓库
 */
class AttendanceApplication : Application() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "attendance_db"
        ).build()
    }

    val workerRepository: WorkerRepository by lazy {
        WorkerRepository(database.workerDao())
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepository(database.attendanceDao())
    }

    companion object {
        lateinit var instance: AttendanceApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
