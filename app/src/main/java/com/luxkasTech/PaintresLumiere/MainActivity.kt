package com.luxkasTech.PaintresLumiere

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.luxkasTech.PaintresLumiere.core.messages.MessagesService
import com.luxkasTech.PaintresLumiere.core.messages.ToastHost
import com.luxkasTech.PaintresLumiere.navigation.AppNavHost
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single activity. It only hosts the Compose tree; every screen and every navigation decision
 * lives under [AppNavHost].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var messages: MessagesService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is dark-only, so system bar icons are always light regardless of system theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            PaintresLumiereTheme {
                Box(Modifier.fillMaxSize()) {
                    AppNavHost()
                    ToastHost(messages, Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}
