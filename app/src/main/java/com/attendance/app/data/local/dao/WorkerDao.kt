package com.attendance.app.data.local.dao

import androidx.room.*
import com.attendance.app.data.local.entity.Worker
import kotlinx.coroutines.flow.Flow

/**
 * 工人数据访问对象
 */
@Dao
interface WorkerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(worker: Worker): Long

    @Update
    suspend fun update(worker: Worker)

    @Delete
    suspend fun delete(worker: Worker)

    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: Long): Worker?

    @Query("SELECT * FROM workers WHERE name LIKE '%' || :keyword || '%' ORDER BY name ASC")
    fun searchWorkers(keyword: String): Flow<List<Worker>>

    @Query("SELECT COUNT(*) FROM workers")
    suspend fun getCount(): Int
}
