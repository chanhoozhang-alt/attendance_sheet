# 考勤表 - 工地考勤与工资计算 App

一款本地离线的 Android 考勤管理应用，用于记录工人每日考勤、自动统计出勤情况、计算工资并导出 Excel 报表。

**[下载 APK](https://github.com/chanhoozhang-alt/attendance_sheet/releases/tag/v1.0.0)**

## 功能特性

- 工人管理 - 添加/编辑/删除工人信息
- 考勤记录 - 记录每日上下午出勤和加班时长
- 工资计算 - 自动根据出勤天数和加班时长计算工资
- 数据统计 - 按月/年查看统计数据
- Excel 导出 - 导出考勤记录和工资报表

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose
- **数据库**: Room (SQLite)
- **Excel 导出**: Apache POI
- **最低版本**: Android 8.0+

## 安装

点击上方下载链接获取 APK，直接安装即可。

## 使用说明

1. 首页添加工人，设置日工资和加班时薪
2. 点击工人进入详情页，通过日历录入考勤
3. 在统计页查看月度/年度数据
4. 可导出 Excel 报表分享或存档

## 数据存储

所有数据存储在本地 SQLite 数据库，无需联网，保护隐私。

## License

MIT License
