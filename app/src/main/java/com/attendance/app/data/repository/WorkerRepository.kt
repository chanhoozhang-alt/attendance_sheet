package com.attendance.app.data.repository

import com.attendance.app.data.local.dao.WorkerDao
import com.attendance.app.data.local.entity.Worker
import kotlinx.coroutines.flow.Flow

/**
 * 工人数据仓库
 */
class WorkerRepository(private val workerDao: WorkerDao) {

    fun getAllWorkers(): Flow<List<Worker>> = workerDao.getAllWorkers()

    suspend fun getWorkerById(id: Long): Worker? = workerDao.getWorkerById(id)

    fun searchWorkers(keyword: String): Flow<List<Worker>> = workerDao.searchWorkers(keyword)

    suspend fun insertWorker(worker: Worker): Long = workerDao.insert(worker)

    suspend fun updateWorker(worker: Worker) = workerDao.update(worker)

    suspend fun deleteWorker(worker: Worker) = workerDao.delete(worker)
}
