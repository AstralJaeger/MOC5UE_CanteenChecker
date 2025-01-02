package com.example.canteenchecker.adminapp.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

private const val REVIEW_CHANGED_INTENT_ACTION = "ReviewChanged"
private const val REVIEW_CHANGED_INTENT_CANTEEN_ID_KEY = "id"

fun Context.sendReviewChangedBroadcast(id: String) {
    LocalBroadcastManager.getInstance(this)
        .sendBroadcast(Intent(REVIEW_CHANGED_INTENT_ACTION).apply {
            putExtra(REVIEW_CHANGED_INTENT_CANTEEN_ID_KEY, id)
        })
}

fun Context.registerReviewChangedBroadcastReceiver(broadcastReceiver: ReviewChangedBroadcastReceiver) {
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(broadcastReceiver, IntentFilter(REVIEW_CHANGED_INTENT_ACTION))
}

fun Context.unregisterReviewChangedBroadcastReceiver(broadcastReceiver: ReviewChangedBroadcastReceiver) {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
}

abstract class ReviewChangedBroadcastReceiver : BroadcastReceiver() {

    final override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(REVIEW_CHANGED_INTENT_CANTEEN_ID_KEY)?.let {
            onReceiveReviewChanged(it)
        }
    }

    abstract fun onReceiveReviewChanged(id: String)
}