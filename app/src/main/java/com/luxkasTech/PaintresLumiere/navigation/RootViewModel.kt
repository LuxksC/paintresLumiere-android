package com.luxkasTech.PaintresLumiere.navigation

import androidx.lifecycle.ViewModel
import com.luxkasTech.PaintresLumiere.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    sessionManager: SessionManager,
) : ViewModel() {
    val isAuthenticated: StateFlow<Boolean> = sessionManager.isAuthenticated
}
