package com.example.canteenchecker.adminapp.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

private const val CANTEEN_CHANGED_INTENT_ACTION = "CanteenChanged"
private const val CANTEEN_CHANGED_INTENT_CANTEEN_ID_KEY = "id"

fun Context.sendCanteenChangedBroadcast(id: String) {
    LocalBroadcastManager.getInstance(this)
        .sendBroadcast(Intent(CANTEEN_CHANGED_INTENT_ACTION).apply {
            putExtra(CANTEEN_CHANGED_INTENT_CANTEEN_ID_KEY, id)
        })
}

fun Context.registerCanteenChangedBroadcastReceiver(broadcastReceiver: CanteenChangedBroadcastReceiver) {
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(broadcastReceiver, IntentFilter(CANTEEN_CHANGED_INTENT_ACTION))
}

fun Context.unregisterCanteenChangedBroadcastReceiver(broadcastReceiver: CanteenChangedBroadcastReceiver) {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
}

abstract class CanteenChangedBroadcastReceiver : BroadcastReceiver() {

    final override fun onReceive(context: Context, intent: Intent) {
        intent.getStringExtra(CANTEEN_CHANGED_INTENT_CANTEEN_ID_KEY)?.let {
            onReceiveCanteenChanged(it)
        }
    }

    abstract fun onReceiveCanteenChanged(id: String)
}