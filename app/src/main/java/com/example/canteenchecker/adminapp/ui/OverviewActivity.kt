package com.example.canteenchecker.adminapp.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.databinding.ActivityOverviewBinding
import com.example.canteenchecker.consumerapp.api.AdminApiFactory
import kotlinx.coroutines.launch

class OverviewActivity : AppCompatActivity() {

    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load current data from server
        updateCanteenDetails()

        setSupportActionBar(binding.toolbar)
        updateCanteenDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.example.canteenchecker.adminapp.R.menu.overview_menu, menu)
        return true
    }



    private fun updateCanteenDetails() = lifecycleScope.launch {
        // Show loading
        binding.viwProgress.visibility = View.VISIBLE
        binding.viwContent.visibility = View.GONE

        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().getOwnedCanteenDetails(token)
            .onFailure {
                Toast.makeText(
                    this@OverviewActivity,
                    "Error requesting canteen info",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSuccess {
                binding.edtName.setText(it.name, TextView.BufferType.EDITABLE)
                binding.edtAddress.setText(it.address, TextView.BufferType.EDITABLE)
                binding.edtPhoneNumber.setText(it.phoneNumber, TextView.BufferType.EDITABLE)
                binding.edtWebsite.setText(it.website, TextView.BufferType.EDITABLE)
            }

        // Hide loading, show content
        binding.viwProgress.visibility = View.GONE
        binding.viwContent.visibility = View.VISIBLE
    }

    private fun writeCanteenDetails() = lifecycleScope.launch {
        // Optional: show progress indicator while saving
        binding.viwProgress.visibility = View.VISIBLE
        binding.viwContent.visibility = View.GONE

        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().updateCanteenData(
            token,
            binding.edtName.text.toString(),
            binding.edtAddress.text.toString(),
            binding.edtWebsite.text.toString(),
            binding.edtPhoneNumber.text.toString()
        )
            .onFailure {
                Log.e(TAG, "Error updating canteen data")
                Toast.makeText(
                    this@OverviewActivity,
                    "Error updating canteen info",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSuccess {
                Log.v(TAG, "Successfully updated canteen data")
                // If you want to reload updated data from server:
                updateCanteenDetails()
            }
    }
}
