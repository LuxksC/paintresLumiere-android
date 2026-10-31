package com.luxkasTech.PaintresLumiere.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    }
}
