package com.example.glorywisher

import android.app.Application
import com.google.firebase.FirebaseApp

class GreetingFlyerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 