package com.attendance.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.local.entity.Attendance
import com.attendance.app.data.local.entity.Worker
import com.attendance.app.data.repository.AttendanceRepository
import com.attendance.app.data.repository.WorkerRepository
import com.attendance.app.utils.DateUtils
import com.attendance.app.utils.SalaryCalculator
import com.attendance.app.utils.SalaryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 工人列表 ViewModel
 */
class WorkerViewModel(
    private val repository: WorkerRepository
) : ViewModel() {

    val workers: StateFlow<List<Worker>> = repository.getAllWorkers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val searchResults: StateFlow<List<Worker>> = kotlinx.coroutines.flow.combine(
        workers,
        searchQuery
    ) { workerList, query ->
        if (query.isBlank()) {
            workerList
        } else {
            workerList.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertWorker(worker: Worker) {
        viewModelScope.launch {
            try {
                repository.insertWorker(worker)
                _message.value = "添加成功"
            } catch (e: Exception) {
                _message.value = "添加失败: ${e.message}"
            }
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            try {
                repository.deleteWorker(worker)
                _message.value = "删除成功"
            } catch (e: Exception) {
                _message.value = "删除失败: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

/**
 * 工人详情 ViewModel
 */
class WorkerDetailViewModel(
    private val workerId: Long,
    private val workerRepository: WorkerRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _worker = MutableStateFlow<Worker?>(null)
    val worker: StateFlow<Worker?> = _worker

    private val _currentMonth = MutableStateFlow(getCurrentMonth())
    val currentMonth: StateFlow<String> = _currentMonth

    private val _currentYear = MutableStateFlow(getCurrentYear())
    val currentYear: StateFlow<String> = _currentYear.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<Attendance>>(emptyList())
    val attendanceRecords: StateFlow<List<Attendance>> = _attendanceRecords

    private val _yearlyAttendanceRecords = MutableStateFlow<Map<String, List<Attendance>>>(emptyMap())
    val yearlyAttendanceRecords: StateFlow<Map<String, List<Attendance>>> = _yearlyAttendanceRecords.asStateFlow()

    private val _yearlySalaryResults = MutableStateFlow<Map<String, SalaryResult>>(emptyMap())
    val yearlySalaryResults: StateFlow<Map<String, SalaryResult>> = _yearlySalaryResults.asStateFlow()

    private val _salaryResult = MutableStateFlow<SalaryResult?>(null)
    val salaryResult: StateFlow<SalaryResult?> = _salaryResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        loadWorker()
    }

    private fun getCurrentMonth(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        return "$year-${month.toString().padStart(2, '0')}"
    }

    private fun getCurrentYear(): String {
        val calendar = java.util.Calendar.getInstance()
        return calendar.get(java.util.Calendar.YEAR).toString()
    }

    private fun loadWorker() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _worker.value = workerRepository.getWorkerById(workerId)
                loadAttendanceForMonth()
            } catch (e: Exception) {
                _message.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAttendanceForMonth() {
        viewModelScope.launch {
            val month = _currentMonth.value
            val startDate = DateUtils.getMonthStart(month)
            val endDate = DateUtils.getMonthEnd(month)

            try {
                val records = attendanceRepository.getAttendanceByWorkerAndDateRange(
                    workerId, startDate, endDate
                )
                _attendanceRecords.value = records

                _worker.value?.let { w ->
                    _salaryResult.value = SalaryCalculator.calculateSalary(w, records)
                }
            } catch (e: Exception) {
                _message.value = "加载考勤记录失败: ${e.message}"
            }
        }
    }

    fun setMonth(month: String) {
        _currentMonth.value = month
        loadAttendanceForMonth()
    }

    fun setYear(year: String) {
        _currentYear.value = year
        loadAttendanceForYear()
    }

    fun loadAttendanceForYear() {
        viewModelScope.launch {
            val year = _currentYear.value
            val recordsByMonth = mutableMapOf<String, List<Attendance>>()
            val salaryByMonth = mutableMapOf<String, SalaryResult>()

            _worker.value?.let { w ->
                for (month in 1..12) {
                    val monthStr = "$year-${month.toString().padStart(2, '0')}"
                    val startDate = DateUtils.getMonthStart(monthStr)
                    val endDate = DateUtils.getMonthEnd(monthStr)

                    try {
                        val records = attendanceRepository.getAttendanceByWorkerAndDateRange(
                            workerId, startDate, endDate
                        )
                        if (records.isNotEmpty()) {
                            recordsByMonth[monthStr] = records
                            salaryByMonth[monthStr] = SalaryCalculator.calculateSalary(w, records)
                        }
                    } catch (e: Exception) {
                        // 忽略单月加载错误
                    }
                }

                _yearlyAttendanceRecords.value = recordsByMonth
                _yearlySalaryResults.value = salaryByMonth
            }
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            try {
                workerRepository.updateWorker(worker)
                _worker.value = worker
                _message.value = "保存成功"
            } catch (e: Exception) {
                _message.value = "保存失败: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

/**
 * 考勤录入 ViewModel
 */
class AttendanceViewModel(
    private val workerId: Long,
    private val repository: AttendanceRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _worker = MutableStateFlow<Worker?>(null)
    val worker: StateFlow<Worker?> = _worker

    private val _selectedDate = MutableStateFlow(DateUtils.getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate

    private val _morning = MutableStateFlow(false)
    val morning: StateFlow<Boolean> = _morning

    private val _afternoon = MutableStateFlow(false)
    val afternoon: StateFlow<Boolean> = _afternoon

    private val _overtimeHours = MutableStateFlow("")
    val overtimeHours: StateFlow<String> = _overtimeHours

    private val _remark = MutableStateFlow("")
    val remark: StateFlow<String> = _remark

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing

    init {
        loadWorker()
    }

    private fun loadWorker() {
        viewModelScope.launch {
            _worker.value = workerRepository.getWorkerById(workerId)
        }
    }

    fun setDate(date: String) {
        _selectedDate.value = date
        loadAttendance(date)
    }

    fun setMorning(value: Boolean) {
        _morning.value = value
    }

    fun setAfternoon(value: Boolean) {
        _afternoon.value = value
    }

    fun setOvertimeHours(value: String) {
        _overtimeHours.value = value
    }

    fun setRemark(value: String) {
        _remark.value = value
    }

    private fun loadAttendance(date: String) {
        viewModelScope.launch {
            try {
                val attendance = repository.getAttendanceByWorkerAndDate(workerId, date)
                if (attendance != null) {
                    _morning.value = attendance.morning
                    _afternoon.value = attendance.afternoon
                    _overtimeHours.value = if (attendance.overtimeHours > 0) attendance.overtimeHours.toString() else ""
                    _remark.value = attendance.remark
                    _isEditing.value = true
                } else {
                    _morning.value = false
                    _afternoon.value = false
                    _overtimeHours.value = ""
                    _remark.value = ""
                    _isEditing.value = false
                }
            } catch (e: Exception) {
                _message.value = "加载失败: ${e.message}"
            }
        }
    }

    fun saveAttendance(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val attendance = Attendance(
                    workerId = workerId,
                    date = _selectedDate.value,
                    morning = _morning.value,
                    afternoon = _afternoon.value,
                    overtimeHours = _overtimeHours.value.toDoubleOrNull() ?: 0.0,
                    remark = _remark.value
                )
                repository.insertAttendance(attendance)
                _message.value = "保存成功"
                onSuccess()
            } catch (e: Exception) {
                _message.value = "保存失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

/**
 * WorkerViewModel Factory
 */
class WorkerViewModelFactory(
    private val repository: WorkerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerViewModel::class.java)) {
            return WorkerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * WorkerDetailViewModel Factory
 */
class WorkerDetailViewModelFactory(
    private val workerId: Long,
    private val workerRepository: WorkerRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerDetailViewModel::class.java)) {
            return WorkerDetailViewModel(workerId, workerRepository, attendanceRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * AttendanceViewModel Factory
 */
class AttendanceViewModelFactory(
    private val workerId: Long,
    private val attendanceRepository: AttendanceRepository,
    private val workerRepository: WorkerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            return AttendanceViewModel(workerId, attendanceRepository, workerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
