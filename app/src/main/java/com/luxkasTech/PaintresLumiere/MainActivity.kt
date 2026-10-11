package com.luxkasTech.PaintresLumiere

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.luxkasTech.PaintresLumiere.navigation.AppNavHost
import com.luxkasTech.PaintresLumiere.ui.theme.PaintresLumiereTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity. It only hosts the Compose tree; every screen and every navigation decision
 * lives under [AppNavHost].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaintresLumiereTheme {
                AppNavHost()
            }
        }
    }
}
