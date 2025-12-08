package com.jose.holamundo.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation bar component for the app.
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFF1A1A2E),
        contentColor = Color.White
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            val iconType = if (selected) screen.selectedIcon else screen.unselectedIcon

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                icon = {
                    when (iconType) {
                        is IconType.Vector -> Icon(
                            imageVector = iconType.imageVector,
                            contentDescription = screen.title
                        )
                        is IconType.Drawable -> Icon(
                            painter = painterResource(id = iconType.resId),
                            contentDescription = screen.title
                        )
                    }
                },
                label = {
                    Text(text = screen.title)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF00D09E),
                    selectedTextColor = Color(0xFF00D09E),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFF00D09E).copy(alpha = 0.1f)
                )
            )
        }
    }
}

