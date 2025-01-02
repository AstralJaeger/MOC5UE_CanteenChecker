package com.example.canteenchecker.adminapp.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.databinding.ActivityOverviewBinding
import com.example.canteenchecker.consumerapp.api.AdminApiFactory
import kotlinx.coroutines.launch

class OverviewActivity : AppCompatActivity() {

    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        updateCanteenDetails()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.v(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.overview_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.v(TAG, "onPrepareOptionsMenu")
        menu?.findItem(R.id.action_save)?.isVisible = true

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "onOptionsItemSelected")
        return when (item.itemId) {
            R.id.action_save -> {
                writeCanteenDetails()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProgress(text: String) {
        Log.v(TAG, "show progress bar")
        binding.viwProgressly.visibility = View.VISIBLE
        binding.viwContent.visibility = View.GONE
        binding.tvProgress.text = text
    }

    private fun hideProgress() {
        Log.v(TAG, "hide progress bar")
        binding.viwProgressly.visibility = View.GONE
        binding.viwContent.visibility = View.VISIBLE
    }

    private fun updateCanteenDetails() = lifecycleScope.launch {
        Log.v(TAG, "updateCanteenDetails")
        showProgress("fetching details")
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

        hideProgress()
    }

    private fun writeCanteenDetails() = lifecycleScope.launch {
        Log.v(TAG, "writeCanteenDetails")
        showProgress("updating details")
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
        hideProgress()
    }

    private fun writeDishDetails() = lifecycleScope.launch {
        Log.v(TAG, "writeDishDetails")
        showProgress("updating dish details")
        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().updateCanteenDish(
            token,
            binding.edtDish.text.toString(),
            binding.edtDishPrice.text.toString().toDouble()
        )
            .onFailure {
                Log.e(TAG, "Error updating dish data")
                Toast.makeText(
                    this@OverviewActivity,
                    "Error updating dish info",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSuccess {
                Log.v(TAG, "Successfully updated dish data")
                // If you want to reload updated data from server:
                updateCanteenDetails()
            }
        hideProgress()
    }
}
