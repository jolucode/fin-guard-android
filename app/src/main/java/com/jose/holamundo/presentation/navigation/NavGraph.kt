package com.jose.holamundo.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jose.holamundo.presentation.dashboard.DashboardScreen
import com.jose.holamundo.presentation.home.HomeScreen
import com.jose.holamundo.presentation.settings.SettingsScreen

/**
 * Main navigation graph for the application.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    isServiceEnabled: Boolean,
    onOpenNotificationSettings: () -> Unit,
    onSendTestNotification: () -> Unit,
    onCheckStatus: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                isServiceEnabled = isServiceEnabled,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onSendTestNotification = onSendTestNotification,
                onCheckStatus = onCheckStatus
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onOpenNotificationSettings = onOpenNotificationSettings
            )
        }
    }
}

