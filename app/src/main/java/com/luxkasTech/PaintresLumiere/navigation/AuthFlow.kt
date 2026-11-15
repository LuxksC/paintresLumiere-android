package com.luxkasTech.PaintresLumiere.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.luxkasTech.PaintresLumiere.feature.auth.reset.ResetPasswordRoute
import com.luxkasTech.PaintresLumiere.feature.auth.forgot.ForgotPasswordRoute
import com.luxkasTech.PaintresLumiere.feature.auth.login.LoginRoute
import com.luxkasTech.PaintresLumiere.feature.auth.signup.SignUpRoute
import kotlinx.serialization.Serializable

@Serializable
data object LoginDestination

@Serializable
data object SignUpDestination

@Serializable
data object ForgotPasswordDestination

/** Opened from the reset email: `paintreslumiere://reset-password?token=…`. */
@Serializable
data class ResetPasswordDestination(val token: String = "")

const val RESET_PASSWORD_DEEP_LINK = "paintreslumiere://reset-password"

/**
 * Signed-out flow: Login, Sign up, Forgot password — the Android counterpart of iOS
 * `AuthCoordinator`. Successful sign-in/sign-up is not handled here: it starts the session and
 * [AppNavHost] swaps to the Main flow.
 */
@Composable
fun AuthFlow(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = LoginDestination) {
        composable<LoginDestination> {
            LoginRoute(
                onSignUpRequested = { navController.navigate(SignUpDestination) },
                onForgotPasswordRequested = { navController.navigate(ForgotPasswordDestination) },
            )
        }
        composable<SignUpDestination> {
            SignUpRoute(onBack = { navController.popBackStack() })
        }
        composable<ForgotPasswordDestination> {
            ForgotPasswordRoute(onFinished = { navController.popBackStack() })
        }
        composable<ResetPasswordDestination>(
            deepLinks = listOf(navDeepLink<ResetPasswordDestination>(basePath = RESET_PASSWORD_DEEP_LINK)),
        ) {
            ResetPasswordRoute(
                onDone = {
                    navController.navigate(LoginDestination) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
            )
        }
    }
}
