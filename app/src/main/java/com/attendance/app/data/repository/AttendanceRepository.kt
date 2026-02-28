package com.attendance.app.data.repository

import com.attendance.app.data.local.dao.AttendanceDao
import com.attendance.app.data.local.entity.Attendance
import kotlinx.coroutines.flow.Flow

/**
 * 考勤数据仓库
 */
class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    fun getAttendanceByWorker(workerId: Long): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByWorker(workerId)

    suspend fun getAttendanceByWorkerAndDate(workerId: Long, date: String): Attendance? =
        attendanceDao.getAttendanceByWorkerAndDate(workerId, date)

    suspend fun getAttendanceByWorkerAndDateRange(
        workerId: Long,
        startDate: String,
        endDate: String
    ): List<Attendance> = attendanceDao.getAttendanceByWorkerAndDateRange(workerId, startDate, endDate)

    suspend fun getAttendanceByDateRange(startDate: String, endDate: String): List<Attendance> =
        attendanceDao.getAttendanceByDateRange(startDate, endDate)

    suspend fun insertAttendance(attendance: Attendance): Long = attendanceDao.insert(attendance)

    suspend fun updateAttendance(attendance: Attendance) = attendanceDao.update(attendance)

    suspend fun deleteAttendance(attendance: Attendance) = attendanceDao.delete(attendance)

    suspend fun deleteAttendanceByWorker(workerId: Long) = attendanceDao.deleteByWorker(workerId)
}
