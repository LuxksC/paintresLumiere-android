package com.luxkasTech.PaintresLumiere.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Root of the app — the Android counterpart of the iOS `AppCoordinator`.
 *
 * It swaps between the Auth flow and the Main flow purely from [RootViewModel.isAuthenticated].
 * Each flow owns its own NavController, so switching flows throws the previous back stack away:
 * after login, Back from Home leaves the app instead of returning to Login, and after logout there
 * is no way back into the signed-in screens.
 *
 * Rule: screens never navigate by themselves. A screen exposes callbacks named after the event
 * (`onProductSelected(sku)`, `onSignUpRequested()`), and only the flow graphs decide where each
 * event leads. Signing in and out never navigates explicitly either — it changes the session, and
 * this composable reacts.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    viewModel: RootViewModel = hiltViewModel(),
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    Crossfade(
        targetState = isAuthenticated,
        animationSpec = tween(300),
        modifier = modifier,
        label = "rootFlow",
    ) { signedIn ->
        if (signedIn) {
            MainFlow(isAdmin = isAdmin, onAdminSectionSelected = viewModel::onAdminSectionSelected)
        } else {
            AuthFlow()
        }
    }
}
