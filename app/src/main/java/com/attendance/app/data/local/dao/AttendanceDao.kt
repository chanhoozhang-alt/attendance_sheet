package com.attendance.app.data.local.dao

import androidx.room.*
import com.attendance.app.data.local.entity.Attendance
import kotlinx.coroutines.flow.Flow

/**
 * 考勤数据访问对象
 */
@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: Attendance): Long

    @Update
    suspend fun update(attendance: Attendance)

    @Delete
    suspend fun delete(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE workerId = :workerId ORDER BY date DESC")
    fun getAttendanceByWorker(workerId: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND date = :date")
    suspend fun getAttendanceByWorkerAndDate(workerId: Long, date: String): Attendance?

    @Query("SELECT * FROM attendance WHERE workerId = :workerId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getAttendanceByWorkerAndDateRange(
        workerId: Long,
        startDate: String,
        endDate: String
    ): List<Attendance>

    @Query("SELECT * FROM attendance WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getAttendanceByDateRange(startDate: String, endDate: String): List<Attendance>

    @Query("DELETE FROM attendance WHERE workerId = :workerId")
    suspend fun deleteByWorker(workerId: Long)

    @Query("SELECT COUNT(*) FROM attendance WHERE workerId = :workerId AND date BETWEEN :startDate AND :endDate")
    suspend fun getCountByWorkerAndDateRange(workerId: Long, startDate: String, endDate: String): Int
}
