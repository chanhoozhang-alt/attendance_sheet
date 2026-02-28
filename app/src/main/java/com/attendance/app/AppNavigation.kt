package com.attendance.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.attendance.app.ui.about.AboutScreen
import com.attendance.app.ui.home.HomeScreen
import com.attendance.app.ui.statistics.StatisticsScreen
import com.attendance.app.ui.worker.AttendanceScreen
import com.attendance.app.ui.worker.WorkerDetailScreen

/**
 * 导航路由
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object WorkerDetail : Screen("worker_detail/{workerId}") {
        fun createRoute(workerId: Long) = "worker_detail/$workerId"
    }
    object Attendance : Screen("attendance/{workerId}?date={date}") {
        fun createRoute(workerId: Long, date: String? = null): String {
            return if (date != null) {
                "attendance/$workerId?date=$date"
            } else {
                "attendance/$workerId"
            }
        }
    }
    object Statistics : Screen("statistics")
    object About : Screen("about")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onWorkerClick = { workerId ->
                    navController.navigate(Screen.WorkerDetail.createRoute(workerId))
                },
                onStatisticsClick = {
                    navController.navigate(Screen.Statistics.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.WorkerDetail.route) { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId")?.toLongOrNull() ?: 0L
            WorkerDetailScreen(
                workerId = workerId,
                onBack = { navController.popBackStack() },
                onAddAttendance = { date ->
                    navController.navigate(Screen.Attendance.createRoute(workerId, date))
                },
                onEditAttendance = { attendanceId, date ->
                    navController.navigate(Screen.Attendance.createRoute(workerId, date))
                }
            )
        }

        composable(Screen.Attendance.route) { backStackEntry ->
            val workerId = backStackEntry.arguments?.getString("workerId")?.toLongOrNull() ?: 0L
            val date = backStackEntry.arguments?.getString("date")
            AttendanceScreen(
                workerId = workerId,
                initialDate = date,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = { navController.popBackStack() },
                onWorkerClick = { workerId ->
                    navController.navigate(Screen.WorkerDetail.createRoute(workerId))
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
