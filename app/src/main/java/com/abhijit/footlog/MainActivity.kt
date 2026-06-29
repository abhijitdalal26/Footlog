package com.abhijit.footlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.abhijit.footlog.data.preferences.AppPreferences
import com.abhijit.footlog.ui.navigation.AppNavHost
import com.abhijit.footlog.ui.navigation.Screen
import com.abhijit.footlog.ui.theme.FootlogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = AppPreferences(this)

        setContent {
            val onboardingDone by prefs.isOnboardingComplete.collectAsState(initial = false)
            val themeMode by prefs.themeMode.collectAsState(initial = "system")

            FootlogTheme(themeMode = themeMode) {
                AppNavHost(startDestination = if (onboardingDone) Screen.Home else Screen.Onboarding)
            }
        }
    }
}
