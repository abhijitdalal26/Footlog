package com.abhijit.footlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.abhijit.footlog.data.preferences.AppPreferences
import com.abhijit.footlog.ui.navigation.AppNavHost
import com.abhijit.footlog.ui.navigation.Screen
import com.abhijit.footlog.ui.theme.FootlogTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPreferences(this)
        lifecycleScope.launch {
            val onboardingDone = prefs.isOnboardingComplete.first()
            setContent {
                FootlogTheme {
                    AppNavHost(startDestination = if (onboardingDone) Screen.Home else Screen.Onboarding)
                }
            }
        }
    }
}
