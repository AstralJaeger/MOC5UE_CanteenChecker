package com.example.canteenchecker.adminapp

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging

class CanteenCheckerAdminApplication: Application() {

//    companion object {
//        private const val REMOTE_MESSAGE_TOPIC = "ReviewUpdates"
//    }

    var authenticationToken: String? = null

//    override fun onCreate() {
//        super.onCreate()
//         FirebaseMessaging.getInstance().subscribeToTopic(REMOTE_MESSAGE_TOPIC)
//    }
}