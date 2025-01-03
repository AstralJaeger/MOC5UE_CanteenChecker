package com.example.canteenchecker.adminapp

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.canteenchecker.adminapp.ui.LoginActivity

object AuthUtils {
    fun checkAuthentication(activity: AppCompatActivity, onAuthenticated: () -> Unit) {
        val app = activity.application as CanteenCheckerAdminApplication
        if (!app.isAuthenticated()) {
            val loginLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    onAuthenticated()
                } else {
                    activity.finish()
                }
            }
            loginLauncher.launch(LoginActivity.intent(activity))
        } else {
            onAuthenticated()
        }
    }
}