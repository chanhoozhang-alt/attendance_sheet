package com.attendance.app.ui.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attendance.app.AttendanceApplication
import com.attendance.app.data.local.entity.Worker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWorkerClick: (Long) -> Unit,
    onStatisticsClick: () -> Unit,
    onAboutClick: () -> Unit,
    viewModel: WorkerViewModel = viewModel(
        factory = WorkerViewModelFactory(AttendanceApplication.instance.workerRepository)
    )
) {
    val workers by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val message by viewModel.message.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Worker?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("员工考勤") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onStatisticsClick) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "统计",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onAboutClick) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "关于",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加员工")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索员工") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )

            // 员工列表
            if (workers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PersonOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "暂无员工，点击右下角按钮添加",
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workers, key = { it.id }) { worker ->
                        WorkerCard(
                            worker = worker,
                            onClick = { onWorkerClick(worker.id) },
                            onLongClick = { showDeleteDialog = worker }
                        )
                    }
                }
            }
        }
    }

    // 添加员工对话框
    if (showAddDialog) {
        AddWorkerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, dailyWage, overtimeWage, remark ->
                val worker = Worker(
                    name = name,
                    dailyWage = dailyWage,
                    overtimeHourlyWage = overtimeWage,
                    remark = remark
                )
                viewModel.insertWorker(worker)
                showAddDialog = false
            }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { worker ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除员工 \"${worker.name}\" 吗？相关考勤记录也会被删除。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorker(worker)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun WorkerCard(
    worker: Worker,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
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
            // 头像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = worker.name.firstOrNull()?.toString() ?: "?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = worker.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "日工资: ¥${worker.dailyWage} | 加班时薪: ¥${worker.overtimeHourlyWage}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // 箭头
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun AddWorkerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, dailyWage: Double, overtimeWage: Double, remark: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dailyWage by remember { mutableStateOf("") }
    var overtimeWage by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var wageError by remember { mutableStateOf(false) }
    var wageValueError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加员工") },
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
                    label = { Text("姓名 *") },
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
                    label = { Text("日工资 *") },
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
