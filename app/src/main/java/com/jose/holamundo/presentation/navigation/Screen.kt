package com.jose.holamundo.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.jose.holamundo.R

/**
 * Sealed interface for icon types - supports both ImageVector and drawable resources.
 */
sealed interface IconType {
    data class Vector(val imageVector: ImageVector) : IconType
    data class Drawable(@DrawableRes val resId: Int) : IconType
}

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: IconType,
    val unselectedIcon: IconType
) {
    data object Home : Screen(
        route = "home",
        title = "Inicio",
        selectedIcon = IconType.Vector(Icons.Filled.Home),
        unselectedIcon = IconType.Vector(Icons.Outlined.Home)
    )

    data object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = IconType.Drawable(R.drawable.dashboard),
        unselectedIcon = IconType.Drawable(R.drawable.dashboard)
    )

    data object Settings : Screen(
        route = "settings",
        title = "Ajustes",
        selectedIcon = IconType.Vector(Icons.Filled.Settings),
        unselectedIcon = IconType.Vector(Icons.Outlined.Settings)
    )

    companion object {
        val bottomNavItems = listOf(Home, Dashboard, Settings)
    }
}

