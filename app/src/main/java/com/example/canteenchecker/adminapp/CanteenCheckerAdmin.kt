package com.example.canteenchecker.adminapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class CanteenCheckerAdminApplication: Application() {

    companion object {
        private const val REMOTE_MESSAGE_TOPIC = "CanteenUpdates"
    }

    var authenticationToken: String? = null

    fun isAuthenticated() = authenticationToken != null

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseMessaging.getInstance().subscribeToTopic(REMOTE_MESSAGE_TOPIC)
    }
}