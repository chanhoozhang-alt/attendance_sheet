package com.attendance.app.utils

import android.content.Context
import android.content.Intent
import com.attendance.app.data.local.entity.Attendance
import com.attendance.app.data.local.entity.Worker
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Excel 导出工具类
 */
object ExcelExporter {

    /**
     * 导出单个工人的考勤记录和工资统计
     * @return Result<File> 成功返回文件，失败返回异常信息
     */
    fun exportWorkerAttendance(
        context: Context,
        worker: Worker,
        records: List<Attendance>,
        salary: SalaryResult,
        month: String
    ): Result<File> {
        return try {
            val workbook = XSSFWorkbook()

            // 创建考勤记录表
            val attendanceSheet = workbook.createSheet("考勤记录")

            // 表头
            val headerRow = attendanceSheet.createRow(0)
            headerRow.createCell(0).setCellValue("日期")
            headerRow.createCell(1).setCellValue("上午")
            headerRow.createCell(2).setCellValue("下午")
            headerRow.createCell(3).setCellValue("加班(小时)")
            headerRow.createCell(4).setCellValue("备注")

            // 数据行
            records.forEachIndexed { index, record ->
                val row = attendanceSheet.createRow(index + 1)
                row.createCell(0).setCellValue(DateUtils.formatDisplayDate(record.date))
                row.createCell(1).setCellValue(if (record.morning) "√" else "")
                row.createCell(2).setCellValue(if (record.afternoon) "√" else "")
                row.createCell(3).setCellValue(record.overtimeHours)
                row.createCell(4).setCellValue(record.remark)
            }

            // 设置列宽
            attendanceSheet.setColumnWidth(0, 4000)
            attendanceSheet.setColumnWidth(1, 2000)
            attendanceSheet.setColumnWidth(2, 2000)
            attendanceSheet.setColumnWidth(3, 3000)
            attendanceSheet.setColumnWidth(4, 4000)

            // 创建工资统计表
            val salarySheet = workbook.createSheet("工资统计")

            salarySheet.createRow(0).createCell(0).setCellValue("姓名")
            salarySheet.getRow(0).createCell(1).setCellValue(worker.name)

            salarySheet.createRow(1).createCell(0).setCellValue("统计月份")
            salarySheet.getRow(1).createCell(1).setCellValue(month)

            salarySheet.createRow(2).createCell(0).setCellValue("日工资标准")
            salarySheet.getRow(2).createCell(1).setCellValue(worker.dailyWage)

            salarySheet.createRow(3).createCell(0).setCellValue("加班时薪标准")
            salarySheet.getRow(3).createCell(1).setCellValue(worker.overtimeHourlyWage)

            salarySheet.createRow(4).createCell(0).setCellValue("全勤天数")
            salarySheet.getRow(4).createCell(1).setCellValue(salary.fullDays.toDouble())

            salarySheet.createRow(5).createCell(0).setCellValue("半天出勤")
            salarySheet.getRow(5).createCell(1).setCellValue(salary.halfDays.toDouble())

            salarySheet.createRow(6).createCell(0).setCellValue("加班时长(小时)")
            salarySheet.getRow(6).createCell(1).setCellValue(salary.overtimeHours)

            salarySheet.createRow(7).createCell(0).setCellValue("总工资")
            salarySheet.getRow(7).createCell(1).setCellValue(salary.totalSalary)

            // 设置列宽
            salarySheet.setColumnWidth(0, 4000)
            salarySheet.setColumnWidth(1, 4000)

            // 保存文件
            val fileName = "考勤_${worker.name}_$month.xlsx"
            val externalDir = context.getExternalFilesDir(null)
                ?: return Result.failure(IOException("无法访问外部存储目录"))

            val file = File(externalDir, fileName)

            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
            workbook.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(ExportException("导出考勤记录失败: ${e.message}", e))
        }
    }

    /**
     * 批量导出多个工人的工资汇总
     * @return Result<File> 成功返回文件，失败返回异常信息
     */
    fun exportSalarySummary(
        context: Context,
        data: List<Triple<Worker, List<Attendance>, SalaryResult>>,
        month: String
    ): Result<File> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("工资汇总")

            // 表头
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("姓名")
            headerRow.createCell(1).setCellValue("全勤天数")
            headerRow.createCell(2).setCellValue("半天出勤")
            headerRow.createCell(3).setCellValue("加班(小时)")
            headerRow.createCell(4).setCellValue("日工资标准")
            headerRow.createCell(5).setCellValue("加班时薪")
            headerRow.createCell(6).setCellValue("总工资")
            headerRow.createCell(7).setCellValue("备注")

            // 数据行
            data.forEachIndexed { index, (worker, _, salary) ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(worker.name)
                row.createCell(1).setCellValue(salary.fullDays.toDouble())
                row.createCell(2).setCellValue(salary.halfDays.toDouble())
                row.createCell(3).setCellValue(salary.overtimeHours)
                row.createCell(4).setCellValue(worker.dailyWage)
                row.createCell(5).setCellValue(worker.overtimeHourlyWage)
                row.createCell(6).setCellValue(salary.totalSalary)
                row.createCell(7).setCellValue(worker.remark)
            }

            // 设置列宽
            for (i in 0..7) {
                sheet.setColumnWidth(i, 3000)
            }

            // 保存文件
            val fileName = "工资汇总_$month.xlsx"
            val externalDir = context.getExternalFilesDir(null)
                ?: return Result.failure(IOException("无法访问外部存储目录"))

            val file = File(externalDir, fileName)

            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
            workbook.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(ExportException("导出工资汇总失败: ${e.message}", e))
        }
    }

    /**
     * 导出单个工人整年的考勤记录和工资统计
     * @return Result<File> 成功返回文件，失败返回异常信息
     */
    fun exportWorkerAttendanceYearly(
        context: Context,
        worker: Worker,
        recordsByMonth: Map<String, List<Attendance>>,
        salaryByMonth: Map<String, SalaryResult>,
        year: String
    ): Result<File> {
        return try {
            val workbook = XSSFWorkbook()

            // 创建年度汇总表
            val summarySheet = workbook.createSheet("年度汇总")

            summarySheet.createRow(0).createCell(0).setCellValue("姓名")
            summarySheet.getRow(0).createCell(1).setCellValue(worker.name)

            summarySheet.createRow(1).createCell(0).setCellValue("统计年份")
            summarySheet.getRow(1).createCell(1).setCellValue(year)

            summarySheet.createRow(2).createCell(0).setCellValue("日工资标准")
            summarySheet.getRow(2).createCell(1).setCellValue(worker.dailyWage)

            summarySheet.createRow(3).createCell(0).setCellValue("加班时薪标准")
            summarySheet.getRow(3).createCell(1).setCellValue(worker.overtimeHourlyWage)

            // 计算年度总计
            var totalFullDays = 0
            var totalHalfDays = 0
            var totalOvertimeHours = 0.0
            var totalSalary = 0.0

            salaryByMonth.values.forEach { salary ->
                totalFullDays += salary.fullDays
                totalHalfDays += salary.halfDays
                totalOvertimeHours += salary.overtimeHours
                totalSalary += salary.totalSalary
            }

            summarySheet.createRow(4).createCell(0).setCellValue("全年全勤天数")
            summarySheet.getRow(4).createCell(1).setCellValue(totalFullDays.toDouble())

            summarySheet.createRow(5).createCell(0).setCellValue("全年半天出勤")
            summarySheet.getRow(5).createCell(1).setCellValue(totalHalfDays.toDouble())

            summarySheet.createRow(6).createCell(0).setCellValue("全年加班时长(小时)")
            summarySheet.getRow(6).createCell(1).setCellValue(totalOvertimeHours)

            summarySheet.createRow(7).createCell(0).setCellValue("全年总工资")
            summarySheet.getRow(7).createCell(1).setCellValue(totalSalary)

            // 设置列宽
            summarySheet.setColumnWidth(0, 5000)
            summarySheet.setColumnWidth(1, 4000)

            // 创建月度明细表
            val monthlySheet = workbook.createSheet("月度明细")

            val headerRow = monthlySheet.createRow(0)
            headerRow.createCell(0).setCellValue("月份")
            headerRow.createCell(1).setCellValue("全勤天数")
            headerRow.createCell(2).setCellValue("半天出勤")
            headerRow.createCell(3).setCellValue("加班(小时)")
            headerRow.createCell(4).setCellValue("月工资")

            val months = recordsByMonth.keys.sorted()
            months.forEachIndexed { index, month ->
                val row = monthlySheet.createRow(index + 1)
                val salary = salaryByMonth[month]!!
                row.createCell(0).setCellValue(month)
                row.createCell(1).setCellValue(salary.fullDays.toDouble())
                row.createCell(2).setCellValue(salary.halfDays.toDouble())
                row.createCell(3).setCellValue(salary.overtimeHours)
                row.createCell(4).setCellValue(salary.totalSalary)
            }

            // 设置列宽
            for (i in 0..4) {
                monthlySheet.setColumnWidth(i, 3500)
            }

            // 创建考勤明细表
            val attendanceSheet = workbook.createSheet("考勤明细")

            val attHeaderRow = attendanceSheet.createRow(0)
            attHeaderRow.createCell(0).setCellValue("日期")
            attHeaderRow.createCell(1).setCellValue("上午")
            attHeaderRow.createCell(2).setCellValue("下午")
            attHeaderRow.createCell(3).setCellValue("加班(小时)")
            attHeaderRow.createCell(4).setCellValue("备注")

            var rowIndex = 1
            months.forEach { month ->
                val records = recordsByMonth[month] ?: emptyList()
                records.forEach { record ->
                    val row = attendanceSheet.createRow(rowIndex++)
                    row.createCell(0).setCellValue(DateUtils.formatDisplayDate(record.date))
                    row.createCell(1).setCellValue(if (record.morning) "√" else "")
                    row.createCell(2).setCellValue(if (record.afternoon) "√" else "")
                    row.createCell(3).setCellValue(record.overtimeHours)
                    row.createCell(4).setCellValue(record.remark)
                }
            }

            // 设置列宽
            attendanceSheet.setColumnWidth(0, 4000)
            attendanceSheet.setColumnWidth(1, 2000)
            attendanceSheet.setColumnWidth(2, 2000)
            attendanceSheet.setColumnWidth(3, 3000)
            attendanceSheet.setColumnWidth(4, 4000)

            // 保存文件
            val fileName = "考勤_${worker.name}_$year.xlsx"
            val externalDir = context.getExternalFilesDir(null)
                ?: return Result.failure(IOException("无法访问外部存储目录"))

            val file = File(externalDir, fileName)

            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
            workbook.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(ExportException("导出年度考勤记录失败: ${e.message}", e))
        }
    }

    /**
     * 分享导出的文件
     */
    fun shareFile(context: Context, file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "分享考勤文件"))
    }

    /**
     * 自定义导出异常
     */
    class ExportException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
