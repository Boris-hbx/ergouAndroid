package com.ergou.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ergou.app.ui.chat.ChatScreen
import com.ergou.app.ui.chat.ChatViewModel
import com.ergou.app.ui.chat.SessionHistoryScreen
import com.ergou.app.ui.english.EnglishScreen
import com.ergou.app.ui.hub.FeatureHubScreen
import com.ergou.app.ui.expense.ExpenseScreen
import com.ergou.app.ui.health.HealthScreen
import com.ergou.app.ui.memory.MemoryScreen
import com.ergou.app.ui.routine.RoutineScreen
import com.ergou.app.ui.settings.SettingsScreen
import com.ergou.app.ui.soul.SoulScreen
import com.ergou.app.ui.task.TaskScreen
import com.ergou.app.util.NextAuthProvider
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ErgouNavigation() {
    val navController = rememberNavController()
    // Activity scope 共享的 ChatViewModel，ChatScreen 和 SessionHistoryScreen 都使用同一个实例
    val chatViewModel: ChatViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = "chat") {
        composable("chat") {
            ChatScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToFeature = { route -> navController.navigate(route) },
                onNavigateToSessionHistory = { navController.navigate("session-history") },
                onNavigateToFeatureHub = { navController.navigate("feature-hub") },
                viewModel = chatViewModel
            )
        }
        composable("session-history") {
            SessionHistoryScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() },
                onSelectSession = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMemory = { navController.navigate("memory") },
                onNavigateToSoul = { navController.navigate("soul") }
            )
        }
        composable("soul") {
            SoulScreen(onBack = { navController.popBackStack() })
        }
        composable("memory") {
            MemoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("task") {
            TaskScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("routine") {
            RoutineScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("expense") {
            val authProvider: NextAuthProvider = koinInject()
            val sessionToken by authProvider.sessionToken.collectAsState(initial = "")
            ExpenseScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") },
                sessionToken = sessionToken
            )
        }
        composable("english") {
            EnglishScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("trip") {
            com.ergou.app.ui.trip.TripScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("feature-hub") {
            FeatureHubScreen(
                onBack = { navController.popBackStack() },
                onNavigateToFeature = { route -> navController.navigate(route) }
            )
        }
        composable("health") {
            HealthScreen(onBack = { navController.popBackStack() })
        }
    }
}
