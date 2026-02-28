package com.attendance.app.ui.about

import android.content.Context
import android.content.pm.PackageManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val versionName = getVersionName(context)
    var showDisclaimer by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // App信息
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.EventNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "员工考勤",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "版本 $versionName",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // 功能介绍
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "主要功能",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureItem(Icons.Default.Person, "员工管理", "添加、编辑员工信息")
                    FeatureItem(Icons.Default.CalendarToday, "考勤记录", "记录每日出勤和加班")
                    FeatureItem(Icons.Default.Calculate, "工资计算", "自动计算月工资")
                    FeatureItem(Icons.Default.Download, "数据导出", "导出Excel考勤表和工资表")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 重要提醒
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "重要提醒",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            "数据存储在手机本地，请定期导出备份！",
                            fontSize = 14.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 免责声明按钮
            OutlinedButton(
                onClick = { showDisclaimer = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("查看免责声明")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 版权信息
            Text(
                "© 2024 员工考勤 App",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }

    // 免责声明对话框
    if (showDisclaimer) {
        DisclaimerDialog(
            onDismiss = { showDisclaimer = false },
            onConfirm = { showDisclaimer = false }
        )
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DisclaimerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("免责声明") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "数据存储说明",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "本应用所有数据均存储在您的手机本地，不会上传至任何云端服务器。",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "⚠️ 重要提醒",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "以下情况可能导致数据永久丢失：\n" +
                    "• 手机丢失或被盗\n" +
                    "• 手机损坏、系统崩溃\n" +
                    "• 误卸载本应用\n\n" +
                    "请定期导出Excel备份！",
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "责任免除",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• 因设备故障、丢失、误操作等导致的数据丢失，开发者不承担责任\n" +
                    "• 工资计算仅供参考，实际发放请人工核算\n" +
                    "• 使用本应用即表示您已阅读并同意以上条款",
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("我已了解")
            }
        }
    )
}

fun getVersionName(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0"
    }
}
