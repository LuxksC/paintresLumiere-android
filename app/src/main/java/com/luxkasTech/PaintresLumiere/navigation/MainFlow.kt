package com.luxkasTech.PaintresLumiere.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.luxkasTech.PaintresLumiere.R
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLColor
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PLFont
import com.luxkasTech.PaintresLumiere.feature.home.HomeRoute
import kotlinx.serialization.Serializable

// Tab graphs
@Serializable data object HomeGraph
@Serializable data object ProfileGraph

// Destinations
@Serializable data object HomeDestination
@Serializable data class ProductDetailsDestination(val sku: String)
@Serializable data object ProfileDestination

private enum class MainTab(
    val graph: Any,
    /** True for any destination inside this tab's graph. */
    val ownsDestination: (NavDestination) -> Boolean,
    /** True only for the tab's root screen. */
    val isRoot: (NavDestination) -> Boolean,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home(
        HomeGraph,
        { d -> d.hierarchy.any { it.hasRoute<HomeGraph>() } },
        { d -> d.hasRoute<HomeDestination>() },
        R.string.tab_home,
        Icons.Outlined.Home,
        Icons.Filled.Home,
    ),
    Profile(
        ProfileGraph,
        { d -> d.hierarchy.any { it.hasRoute<ProfileGraph>() } },
        { d -> d.hasRoute<ProfileDestination>() },
        R.string.tab_profile,
        Icons.Outlined.Person,
        Icons.Filled.Person,
    ),
}

/**
 * Signed-in flow — the Android counterpart of iOS `MainTabCoordinator`. Each tab is a nested graph
 * with its own back stack, saved and restored when switching tabs. The bottom bar is shown only on
 * the tab roots; pushed screens (product details, …) get the full height.
 */
@Composable
fun MainFlow(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomBar = destination == null || MainTab.entries.any { it.isRoot(destination) }

    Scaffold(
        containerColor = PLColor.backgroundPrimary,
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(destination) { tab ->
                    navController.navigate(tab.graph) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
            NavHost(navController = navController, startDestination = HomeGraph) {
                navigation<HomeGraph>(startDestination = HomeDestination) {
                    composable<HomeDestination> {
                        HomeRoute(onProductSelected = { sku -> navController.navigate(ProductDetailsDestination(sku)) })
                    }
                    composable<ProductDetailsDestination> { FlowPlaceholder() }
                }
                navigation<ProfileGraph>(startDestination = ProfileDestination) {
                    composable<ProfileDestination> { FlowPlaceholder() }
                }
            }
        }
    }
}

@Composable
private fun MainBottomBar(destination: NavDestination?, onSelect: (MainTab) -> Unit) {
    NavigationBar(containerColor = PLColor.backgroundSecondary) {
        MainTab.entries.forEach { tab ->
            val selected = destination?.let(tab.ownsDestination) == true
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onSelect(tab) },
                icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes), style = PLFont.tabLabel) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PLColor.goldBright,
                    selectedTextColor = PLColor.goldBright,
                    indicatorColor = PLColor.backgroundHighlight,
                    unselectedIconColor = PLColor.textMuted,
                    unselectedTextColor = PLColor.textMuted,
                ),
            )
        }
    }
}
