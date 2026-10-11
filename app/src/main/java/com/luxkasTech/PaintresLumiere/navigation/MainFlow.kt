package com.luxkasTech.PaintresLumiere.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/** Signed-in flow: the bottom-navigation tabs and everything pushed from them. */
@Composable
fun MainFlow(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            FlowPlaceholder()
        }
    }
}
