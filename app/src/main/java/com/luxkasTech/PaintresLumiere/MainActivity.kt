package com.luxkasTech.PaintresLumiere

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.luxkasTech.PaintresLumiere.navigation.AppNavHost
import com.luxkasTech.PaintresLumiere.core.designsystem.theme.PaintresLumiereTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity. It only hosts the Compose tree; every screen and every navigation decision
 * lives under [AppNavHost].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is dark-only, so system bar icons are always light regardless of system theme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            PaintresLumiereTheme {
                AppNavHost()
            }
        }
    }
}
