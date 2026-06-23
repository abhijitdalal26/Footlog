package com.abhijit.footlog

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

class FootlogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Crash reporting enabled in release builds, disabled in debug so crashes surface normally
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
