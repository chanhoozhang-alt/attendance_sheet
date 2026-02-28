package com.attendance.app.ui.statistics

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendance.app.AttendanceApplication
import com.attendance.app.data.local.entity.Attendance
import com.attendance.app.data.local.entity.Worker
import com.attendance.app.utils.AppColors
import com.attendance.app.utils.DateUtils
import com.attendance.app.utils.ExcelExporter
import com.attendance.app.utils.SalaryCalculator
import com.attendance.app.utils.SalaryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class WorkerStatistics(
    val worker: Worker,
    val records: List<Attendance>,
    val salary: SalaryResult
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    onWorkerClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentMonth by remember {
        mutableStateOf(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")))
    }

    var statistics by remember { mutableStateOf<List<WorkerStatistics>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }

    // 加载统计数据
    LaunchedEffect(currentMonth) {
        isLoading = true
        withContext(Dispatchers.IO) {
            try {
                val startDate = DateUtils.getMonthStart(currentMonth)
                val endDate = DateUtils.getMonthEnd(currentMonth)

                val workers = AttendanceApplication.instance.workerRepository.getAllWorkers()
                    .first()

                val stats = workers.map { worker ->
                    val records = AttendanceApplication.instance.attendanceRepository
                        .getAttendanceByWorkerAndDateRange(worker.id, startDate, endDate)
                    val salary = SalaryCalculator.calculateSalary(worker, records)
                    WorkerStatistics(worker, records, salary)
                }.sortedByDescending { it.salary.totalSalary }

                withContext(Dispatchers.Main) {
                    statistics = stats
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                    Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 计算总工资
    val totalSalary = statistics.sumOf { it.salary.totalSalary }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
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
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Default.Download, contentDescription = "导出", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 月份选择
            MonthSelectorRow(
                currentMonth = currentMonth,
                onMonthChange = { currentMonth = it }
            )

            // 总工资卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        "$currentMonth 工资总额",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "¥${String.format("%.2f", totalSalary)}",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "共 ${statistics.size} 人",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            // 工资排行
            Text(
                "工资排行",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (statistics.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("暂无数据", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(statistics) { stat ->
                        WorkerStatisticsCard(
                            statistics = stat,
                            rank = statistics.indexOf(stat) + 1,
                            onClick = { onWorkerClick(stat.worker.id) }
                        )
                    }

                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // 导出对话框
    if (showExportDialog && statistics.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出工资汇总") },
            text = { Text("将导出 $currentMonth 所有员工的工资汇总表") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val data = statistics.map { Triple(it.worker, it.records, it.salary) }
                            val result = ExcelExporter.exportSalarySummary(
                                context,
                                data,
                                currentMonth
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
fun MonthSelectorRow(
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val yearMonth = YearMonth.of(year, month).minusMonths(1)
                onMonthChange(yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
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
                val yearMonth = YearMonth.of(year, month).plusMonths(1)
                onMonthChange(yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")))
            }
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
        }
    }
}

@Composable
fun WorkerStatisticsCard(
    statistics: WorkerStatistics,
    rank: Int,
    onClick: () -> Unit
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // 金色
        2 -> Color(0xFFC0C0C0) // 银色
        3 -> Color(0xFFCD7F32) // 铜色
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 排名
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(rankColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    rank.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 头像
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    statistics.worker.name.firstOrNull()?.toString() ?: "?",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    statistics.worker.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        "全勤${statistics.salary.fullDays}天",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "半天${statistics.salary.halfDays}天",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    if (statistics.salary.overtimeHours > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "加班${statistics.salary.overtimeHours}h",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 工资
            Text(
                "¥${String.format("%.2f", statistics.salary.totalSalary)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
