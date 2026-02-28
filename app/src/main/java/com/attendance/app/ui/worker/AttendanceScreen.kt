package com.attendance.app.ui.worker

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attendance.app.AttendanceApplication
import com.attendance.app.data.local.entity.Attendance
import com.attendance.app.ui.home.AttendanceViewModel
import com.attendance.app.ui.home.AttendanceViewModelFactory
import com.attendance.app.utils.AppColors
import com.attendance.app.utils.DateUtils
import com.attendance.app.utils.SalaryCalculator
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    workerId: Long,
    initialDate: String?,
    onBack: () -> Unit,
    viewModel: AttendanceViewModel = viewModel(
        factory = AttendanceViewModelFactory(
            workerId,
            AttendanceApplication.instance.attendanceRepository,
            AttendanceApplication.instance.workerRepository
        )
    )
) {
    val context = LocalContext.current

    val worker by viewModel.worker.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val morning by viewModel.morning.collectAsState()
    val afternoon by viewModel.afternoon.collectAsState()
    val overtimeHours by viewModel.overtimeHours.collectAsState()
    val remark by viewModel.remark.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    // 初始化日期
    LaunchedEffect(initialDate) {
        if (initialDate != null) {
            viewModel.setDate(initialDate)
        }
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val parsedDate = try {
        DateUtils.parseDate(selectedDate)
    } catch (e: Exception) {
        Triple(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    }
    val (year, month, day) = parsedDate

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newDate = DateUtils.formatDate(selectedYear, selectedMonth, selectedDay)
                viewModel.setDate(newDate)
                showDatePicker = false
            },
            year,
            month,
            day
        ).show()
        showDatePicker = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑考勤" else "录入考勤") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 员工信息
            worker?.let { w ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                w.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "日工资: ¥${w.dailyWage} | 加班时薪: ¥${w.overtimeHourlyWage}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // 日期选择
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("日期", fontSize = 16.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            DateUtils.formatDisplayDate(selectedDate),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }

            // 考勤状态
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "出勤状态",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 上午
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = AppColors.HalfDay
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("上午", fontSize = 16.sp)
                        }
                        Switch(
                            checked = morning,
                            onCheckedChange = { viewModel.setMorning(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // 下午
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Brightness5,
                                contentDescription = null,
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("下午", fontSize = 16.sp)
                        }
                        Switch(
                            checked = afternoon,
                            onCheckedChange = { viewModel.setAfternoon(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // 加班时长
            OutlinedTextField(
                value = overtimeHours,
                onValueChange = { viewModel.setOvertimeHours(it) },
                label = { Text("加班时长 (小时)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            )

            // 备注
            OutlinedTextField(
                value = remark,
                onValueChange = { viewModel.setRemark(it) },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Note, contentDescription = null)
                }
            )

            // 当日工资预览
            worker?.let { w ->
                val dailySalary = SalaryCalculator.calculateDailySalary(
                    Attendance(
                        workerId = workerId,
                        date = selectedDate,
                        morning = morning,
                        afternoon = afternoon,
                        overtimeHours = overtimeHours.toDoubleOrNull() ?: 0.0,
                        remark = ""
                    ),
                    w.dailyWage,
                    w.overtimeHourlyWage
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("当日工资", fontSize = 16.sp)
                        Text(
                            "¥${String.format("%.2f", dailySalary)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    viewModel.saveAttendance {
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存", fontSize = 18.sp)
                }
            }
        }
    }
}
