package com.attendance.app.ui.worker

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attendance.app.AttendanceApplication
import com.attendance.app.data.local.entity.Attendance
import com.attendance.app.data.local.entity.Worker
import com.attendance.app.ui.home.*
import com.attendance.app.utils.AppColors
import com.attendance.app.utils.DateUtils
import com.attendance.app.utils.ExcelExporter
import com.attendance.app.utils.SalaryCalculator
import com.attendance.app.utils.SalaryResult
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    workerId: Long,
    onBack: () -> Unit,
    onAddAttendance: (String?) -> Unit,
    onEditAttendance: (Long, String) -> Unit,
    viewModel: WorkerDetailViewModel = viewModel(
        factory = WorkerDetailViewModelFactory(
            workerId,
            AttendanceApplication.instance.workerRepository,
            AttendanceApplication.instance.attendanceRepository
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 监听生命周期，页面恢复时刷新数据
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAttendanceForMonth()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val worker by viewModel.worker.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val currentYear by viewModel.currentYear.collectAsState()
    val message by viewModel.message.collectAsState()
    val attendanceRecords by viewModel.attendanceRecords.collectAsState()
    val salaryResult by viewModel.salaryResult.collectAsState()
    val yearlyAttendanceRecords by viewModel.yearlyAttendanceRecords.collectAsState()
    val yearlySalaryResults by viewModel.yearlySalaryResults.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(worker?.name ?: "员工详情") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = Color.White)
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "导出", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddAttendance(null) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加考勤")
            }
        }
    ) { padding ->
        worker?.let { w ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 工资卡片
                item {
                    SalaryCard(
                        worker = w,
                        salaryResult = salaryResult,
                        month = currentMonth
                    )
                }

                // 月份选择
                item {
                    MonthSelector(
                        currentMonth = currentMonth,
                        onMonthChange = { viewModel.setMonth(it) }
                    )
                }

                // 日历视图
                item {
                    CalendarView(
                        month = currentMonth,
                        attendanceRecords = attendanceRecords,
                        onDayClick = { date, _ ->
                            onAddAttendance(date)
                        }
                    )
                }

                // 考勤记录列表
                item {
                    Text(
                        "考勤记录",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (attendanceRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("本月暂无考勤记录", color = Color.Gray)
                        }
                    }
                } else {
                    items(attendanceRecords.size) { index ->
                        AttendanceRecordItem(
                            attendance = attendanceRecords[index],
                            dailyWage = w.dailyWage,
                            overtimeHourlyWage = w.overtimeHourlyWage,
                            onClick = {
                                onEditAttendance(attendanceRecords[index].id, attendanceRecords[index].date)
                            }
                        )
                    }
                }
            }
        }
    }

    // 编辑员工对话框
    if (showEditDialog && worker != null) {
        EditWorkerDialog(
            worker = worker!!,
            onDismiss = { showEditDialog = false },
            onSave = { name, dailyWage, overtimeWage, remark ->
                viewModel.updateWorker(
                    worker!!.copy(
                        name = name,
                        dailyWage = dailyWage,
                        overtimeHourlyWage = overtimeWage,
                        remark = remark
                    )
                )
                showEditDialog = false
            }
        )
    }

    // 导出对话框
    if (showExportDialog && worker != null) {
        var exportMode by remember { mutableStateOf("month") } // "month" or "year"
        var selectedYear by remember { mutableStateOf(currentYear) }
        var selectedMonth by remember { mutableStateOf(currentMonth) }

        // 选中年份/月份时加载数据
        LaunchedEffect(selectedYear, selectedMonth, exportMode) {
            if (exportMode == "year") {
                viewModel.setYear(selectedYear)
            } else {
                viewModel.setMonth(selectedMonth)
            }
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出 Excel") },
            text = {
                Column {
                    // 导出模式选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = exportMode == "month",
                            onClick = { exportMode = "month" },
                            label = { Text("按月导出") }
                        )
                        FilterChip(
                            selected = exportMode == "year",
                            onClick = { exportMode = "year" },
                            label = { Text("按年导出") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (exportMode == "month") {
                        // 月份选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val parts = selectedMonth.split("-")
                                    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2024
                                    val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
                                    val cal = java.util.Calendar.getInstance().apply {
                                        set(year, month - 1, 1)
                                        add(java.util.Calendar.MONTH, -1)
                                    }
                                    selectedMonth = "${cal.get(java.util.Calendar.YEAR)}-${(cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')}"
                                }
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                            }

                            val parts = selectedMonth.split("-")
                            val displayYear = parts.getOrNull(0) ?: "2024"
                            val displayMonth = parts.getOrNull(1) ?: "01"
                            Text(
                                "$displayYear 年 $displayMonth 月",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = {
                                    val parts = selectedMonth.split("-")
                                    val year = parts.getOrNull(0)?.toIntOrNull() ?: 2024
                                    val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
                                    val cal = java.util.Calendar.getInstance().apply {
                                        set(year, month - 1, 1)
                                        add(java.util.Calendar.MONTH, 1)
                                    }
                                    selectedMonth = "${cal.get(java.util.Calendar.YEAR)}-${(cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')}"
                                }
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("将导出 ${worker!!.name} $selectedMonth 的考勤记录（共 ${attendanceRecords.size} 条）")
                    } else {
                        // 年份选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val yearInt = selectedYear.toIntOrNull() ?: 2024
                                    selectedYear = (yearInt - 1).toString()
                                }
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "上一年")
                            }

                            Text(
                                "$selectedYear 年",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = {
                                    val yearInt = selectedYear.toIntOrNull() ?: 2024
                                    selectedYear = (yearInt + 1).toString()
                                }
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "下一年")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val recordCount = yearlyAttendanceRecords.values.sumOf { it.size }
                        Text("将导出 ${worker!!.name} $selectedYear 年的考勤记录（共 $recordCount 条）")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (exportMode == "month") {
                                val result = ExcelExporter.exportWorkerAttendance(
                                    context,
                                    worker!!,
                                    attendanceRecords,
                                    salaryResult ?: SalaryCalculator.calculateSalary(worker!!, attendanceRecords),
                                    selectedMonth
                                )
                                result.fold(
                                    onSuccess = { file ->
                                        Toast.makeText(context, "导出成功: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                        ExcelExporter.shareFile(context, file)
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(context, "导出失败: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                if (yearlyAttendanceRecords.isEmpty()) {
                                    Toast.makeText(context, "该年份暂无考勤记录", Toast.LENGTH_SHORT).show()
                                } else {
                                    val result = ExcelExporter.exportWorkerAttendanceYearly(
                                        context,
                                        worker!!,
                                        yearlyAttendanceRecords,
                                        yearlySalaryResults,
                                        selectedYear
                                    )
                                    result.fold(
                                        onSuccess = { file ->
                                            Toast.makeText(context, "导出成功: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                            ExcelExporter.shareFile(context, file)
                                        },
                                        onFailure = { error ->
                                            Toast.makeText(context, "导出失败: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                        showExportDialog = false
                    }
                ) {
                    Text("导出并分享")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SalaryCard(
    worker: Worker,
    salaryResult: SalaryResult?,
    month: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$month 工资",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "¥${String.format("%.2f", salaryResult?.totalSalary ?: 0.0)}",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("全勤", "${salaryResult?.fullDays ?: 0}天")
                StatItem("半天", "${salaryResult?.halfDays ?: 0}天")
                StatItem("加班", "${salaryResult?.overtimeHours ?: 0.0}h")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun MonthSelector(
    currentMonth: String,
    onMonthChange: (String) -> Unit
) {
    val parts = try {
        currentMonth.split("-")
    } catch (e: Exception) {
        listOf("2024", "01")
    }

    if (parts.size < 2) return

    val year = parts[0].toIntOrNull() ?: 2024
    val month = parts[1].toIntOrNull() ?: 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val cal = Calendar.getInstance().apply {
                    set(year, month - 1, 1)
                    add(Calendar.MONTH, -1)
                }
                val newMonth = "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}"
                onMonthChange(newMonth)
            }
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
        }

        Text(
            "$year 年 $month 月",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        IconButton(
            onClick = {
                val cal = Calendar.getInstance().apply {
                    set(year, month - 1, 1)
                    add(Calendar.MONTH, 1)
                }
                val newMonth = "${cal.get(Calendar.YEAR)}-${(cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}"
                onMonthChange(newMonth)
            }
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
        }
    }
}

@Composable
fun CalendarView(
    month: String,
    attendanceRecords: List<Attendance>,
    onDayClick: (String, Attendance?) -> Unit
) {
    val parts = try {
        month.split("-")
    } catch (e: Exception) {
        listOf("2024", "01")
    }

    if (parts.size < 2) return

    val year = parts[0].toIntOrNull() ?: 2024
    val monthValue = parts[1].toIntOrNull() ?: 1

    val daysInMonth = DateUtils.getDaysInMonth(year, monthValue - 1)
    val calendar = Calendar.getInstance().apply {
        set(year, monthValue - 1, 1)
    }
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

    val attendanceMap = attendanceRecords.associateBy { it.date }

    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // 星期标题
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 日期格子
            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break

                Row(modifier = Modifier.fillMaxWidth()) {
                    for (day in 0..6) {
                        if ((week == 0 && day < firstDayOfWeek) || dayCounter > daysInMonth) {
                            Box(modifier = Modifier.weight(1f))
                        } else {
                            val dateStr = DateUtils.formatDate(year, monthValue - 1, dayCounter)
                            val attendance = attendanceMap[dateStr]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable { onDayClick(dateStr, attendance) }
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        dayCounter.toString(),
                                        fontSize = 14.sp
                                    )
                                    if (attendance != null) {
                                        Row {
                                            if (attendance.morning) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(
                                                            if (attendance.afternoon) AppColors.FullDay else AppColors.HalfDay,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                            if (attendance.afternoon) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(AppColors.FullDay, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            dayCounter++
                        }
                    }
                }
            }

            // 图例
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.FullDay, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("全天", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AppColors.HalfDay, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("半天", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AttendanceRecordItem(
    attendance: Attendance,
    dailyWage: Double,
    overtimeHourlyWage: Double,
    onClick: () -> Unit
) {
    val dailySalary = SalaryCalculator.calculateDailySalary(attendance, dailyWage, overtimeHourlyWage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    DateUtils.formatDisplayDate(attendance.date),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "上午",
                        fontSize = 12.sp,
                        color = if (attendance.morning) AppColors.FullDay else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "下午",
                        fontSize = 12.sp,
                        color = if (attendance.afternoon) AppColors.FullDay else Color.Gray
                    )
                    if (attendance.overtimeHours > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "加班${attendance.overtimeHours}h",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                "¥${String.format("%.2f", dailySalary)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EditWorkerDialog(
    worker: Worker,
    onDismiss: () -> Unit,
    onSave: (name: String, dailyWage: Double, overtimeWage: Double, remark: String) -> Unit
) {
    var name by remember { mutableStateOf(worker.name) }
    var dailyWage by remember { mutableStateOf(worker.dailyWage.toString()) }
    var overtimeWage by remember { mutableStateOf(worker.overtimeHourlyWage.toString()) }
    var remark by remember { mutableStateOf(worker.remark) }
    var nameError by remember { mutableStateOf(false) }
    var wageError by remember { mutableStateOf(false) }
    var wageValueError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑员工") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("姓名") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("姓名不能为空") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dailyWage,
                    onValueChange = {
                        dailyWage = it
                        wageError = false
                        wageValueError = false
                    },
                    label = { Text("日工资") },
                    isError = wageError || wageValueError,
                    supportingText = when {
                        wageError -> { { Text("日工资不能为空") } }
                        wageValueError -> { { Text("日工资必须大于0") } }
                        else -> null
                    },
                    singleLine = true,
                    prefix = { Text("¥") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = overtimeWage,
                    onValueChange = { overtimeWage = it },
                    label = { Text("加班时薪") },
                    singleLine = true,
                    prefix = { Text("¥") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    nameError = name.isBlank()
                    wageError = dailyWage.isBlank()
                    val dailyWageValue = dailyWage.toDoubleOrNull() ?: 0.0
                    wageValueError = !wageError && dailyWageValue <= 0

                    if (!nameError && !wageError && !wageValueError) {
                        onSave(
                            name.trim(),
                            dailyWageValue,
                            overtimeWage.toDoubleOrNull() ?: 0.0,
                            remark.trim()
                        )
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
