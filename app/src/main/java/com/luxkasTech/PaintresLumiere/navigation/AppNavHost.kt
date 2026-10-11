package com.luxkasTech.PaintresLumiere.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

/** Placeholder start destination until the Auth and Main graphs land. */
@Serializable
data object StartRoute

/**
 * Root of the navigation graph — the Android counterpart of the iOS `AppCoordinator`.
 *
 * Rule: screens never navigate by themselves. A screen exposes callbacks named after the event
 * (`onProductSelected(sku)`, `onLoginSucceeded()`), and only this graph decides where each event
 * leads.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = StartRoute,
        modifier = modifier,
    ) {
        composable<StartRoute> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            )
        }
    }
}
