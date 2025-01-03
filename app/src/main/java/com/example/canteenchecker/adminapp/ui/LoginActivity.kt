package com.example.canteenchecker.adminapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.databinding.ActivityLoginBinding
import com.example.canteenchecker.consumerapp.api.AdminApiFactory
import kotlinx.coroutines.launch

class LoginActivity: AppCompatActivity() {

    private val TAG = this.javaClass.canonicalName

    companion object {
        fun intent(context: Context) = Intent(context, LoginActivity::class.java)
    }

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtUserName.setText("S2210307017")
        binding.edtPassword.setText("S2210307017")
        binding.btnLogIn.setOnClickListener { authenticate() }
    }

    private fun authenticate()  = lifecycleScope.launch{
        setUIEnabled(false)
        AdminApiFactory.createApi().authenticate(binding.edtUserName.text.toString(), binding.edtPassword.text.toString())
            .onFailure {
                setUIEnabled(true)
                 binding.edtPassword.text.clear()
                Toast.makeText(this@LoginActivity, R.string.message_login_failed, Toast.LENGTH_SHORT).show()
            }
            .onSuccess {
                Log.v(TAG, "Authentication successful")

                binding.edtUserName.text.clear()
                binding.edtPassword.text.clear()
                binding.btnLogIn.isEnabled = true
                binding.btnLogIn.clearFocus()

                (application as CanteenCheckerAdminApplication).authenticationToken = it
                val intent = Intent(this@LoginActivity, OverviewActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                setResult(Activity.RESULT_OK)
                finish()
            }
    }

    private fun setUIEnabled(enabled: Boolean){
        binding.btnLogIn.isEnabled = enabled
        binding.edtUserName.isEnabled = enabled
        binding.edtPassword.isEnabled = enabled
    }
}