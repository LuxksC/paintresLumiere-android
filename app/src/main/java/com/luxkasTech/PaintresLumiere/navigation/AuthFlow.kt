package com.luxkasTech.PaintresLumiere.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

/** Signed-out flow: Login, Sign up, Forgot password. */
@Composable
fun AuthFlow(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            FlowPlaceholder()
        }
    }
}
