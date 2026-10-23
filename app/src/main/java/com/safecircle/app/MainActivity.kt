package com.safecircle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.safecircle.app.ui.navigation.SafeCircleNavHost
import com.safecircle.app.ui.theme.SafeCircleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeCircleTheme {
                SafeCircleNavHost()
            }
        }
    }
}
