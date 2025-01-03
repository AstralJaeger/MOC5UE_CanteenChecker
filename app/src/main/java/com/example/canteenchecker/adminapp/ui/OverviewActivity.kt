package com.example.canteenchecker.adminapp.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.AuthUtils
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.core.OwnedCanteenDetails
import com.example.canteenchecker.adminapp.core.sendCanteenChangedBroadcast
import com.example.canteenchecker.adminapp.databinding.ActivityOverviewBinding
import com.example.canteenchecker.consumerapp.api.AdminApiFactory
import kotlinx.coroutines.launch
import java.util.Locale


class OverviewActivity : AppCompatActivity() {

    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityOverviewBinding
    private var canteen: OwnedCanteenDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        binding = ActivityOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.v(TAG, "onBackPressed - preventing further back navigation")
            }
        })

        AuthUtils.checkAuthentication(this) {
            updateCanteenDetails {
                canteen?.let {
                    Log.v(TAG, "inflating canteen reviews fragment")
                    supportFragmentManager.beginTransaction()
                        .add(R.id.fcvReview, ReviewsFragment.newInstance(it.id))
                        .commit()
                    binding.fcvReview.setOnClickListener {
                        startActivity(ReviewsActivity.intent(this, canteen!!.id))
                    }
                } ?: run {
                    Log.e(TAG, "Canteen is null after update")
                    Toast.makeText(this, "Failed to load canteen details.", Toast.LENGTH_SHORT).show()
                    onLogout()
                }
            }
        }

        // TODO: Map

        hideProgress()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.v(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.overview_menu, menu)
        menu.findItem(R.id.action_logout)?.setOnMenuItemClickListener {
            onLogoutConfirmationDialog()
            true
        }

        menu.findItem(R.id.action_save)?.setOnMenuItemClickListener {
            onSave()
            true
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.v(TAG, "onPrepareOptionsMenu")
        menu?.findItem(R.id.action_logout)?.isVisible = true
        menu?.findItem(R.id.action_save)?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "onOptionsItemSelected")
        return super.onOptionsItemSelected(item)
    }

    private fun onLogoutConfirmationDialog() {
        Log.v(TAG, "onLogoutConfirmationDialog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        if (hasPendingChanges()) {
            builder.setMessage("You have unsaved changes. Are you sure you want to logout? They will be lost.")
        } else {
            builder.setMessage("Are you sure you want to logout?")
        }
        builder.setPositiveButton("Yes") { _, _ ->
            onLogout()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun onLogout() {
        Log.v(TAG, "onLogout")
        (application as CanteenCheckerAdminApplication).authenticationToken = null
        startActivity(LoginActivity.intent(this))
    }

    private fun onSave() {
        Log.v(TAG, "onSave")

        if (hasCanteenDetailsChanged()) {
            Log.v(TAG, "Canteen details changed")
            showProgress("updating canteen details")
            writeCanteenDetails()
        }

        if (hasDishDetailsChanged()) {
            Log.v(TAG, "Dish details changed")
            showProgress("updating dish details")
            writeDishDetails()
        }

        if (hasWaitingTimeChanged()) {
            Log.v(TAG, "Waiting time changed")
            showProgress("updating waiting time")
            writeWaitingTime()
        }

        sendCanteenChangedBroadcast(canteen!!.id)
        hideProgress()
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

    private fun hasPendingChanges(): Boolean {
        Log.v(TAG, "hasPendingChanges")
        return hasCanteenDetailsChanged() || hasDishDetailsChanged() || hasWaitingTimeChanged()
    }

    private fun hasCanteenDetailsChanged(): Boolean {
        Log.v(TAG, "hasCanteenDetailsChanged")
        return canteen?.let {
            it.name != binding.edtName.text.toString() ||
            it.phoneNumber != binding.edtPhoneNumber.text.toString() ||
            it.website != binding.edtWebsite.text.toString()
        } ?: false
    }

    private fun updateCanteenDetails(onSuccess: (() -> Unit)? = null) = lifecycleScope.launch {
        Log.v(TAG, "updateCanteenDetails")
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
                canteen = it
                binding.edtName.setText(it.name, TextView.BufferType.EDITABLE)
                binding.edtPhoneNumber.setText(it.phoneNumber, TextView.BufferType.EDITABLE)
                binding.edtWebsite.setText(it.website, TextView.BufferType.EDITABLE)
                binding.edtDish.setText(it.dish, TextView.BufferType.EDITABLE)
                binding.edtDishPrice.setText(String.format(Locale.getDefault(), "%.2f", it.dishPrice), TextView.BufferType.EDITABLE)
                binding.edtWaitingTime.setText(String.format(Locale.getDefault(), "%d", it.waitingTime), TextView.BufferType.EDITABLE)

                onSuccess?.invoke()
            }
    }

    private fun writeCanteenDetails() = lifecycleScope.launch {
        Log.v(TAG, "writeCanteenDetails")
        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().updateCanteenData(
            token,
            binding.edtName.text.toString(),
            canteen!!.address,
            binding.edtWebsite.text.toString(),
            binding.edtPhoneNumber.text.toString(),
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
            }
    }

    private fun hasDishDetailsChanged(): Boolean {
        Log.v(TAG, "hasDishDetailsChanged")
        return canteen?.let {
            it.dish != binding.edtDish.text.toString() ||
            it.dishPrice != binding.edtDishPrice.text.toString().toDouble()
        } ?: false
    }

    private fun alertDishDetailsInvalid() {
        Log.v(TAG, "alertDishDetailsInvalid")
        AlertDialog.Builder(this)
            .setTitle("Invalid dish details")
            .setMessage("Please enter a valid dish name and price. The price must be a number.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun writeDishDetails() = lifecycleScope.launch {
        Log.v(TAG, "writeDishDetails")
        showProgress("updating dish details")
        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!

        // Validate price
        var price = 0.0
        val priceRaw = binding.edtDishPrice.text.toString()
        if (priceRaw.isEmpty() || priceRaw.toDoubleOrNull() == null) {
            alertDishDetailsInvalid()
            return@launch
        }

        try {
            price = priceRaw.toDouble()
        } catch (e: NumberFormatException) {
            alertDishDetailsInvalid()
            return@launch
        }

        AdminApiFactory.createApi().updateCanteenDish(
            token,
            binding.edtDish.text.toString() ,
            price
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

    private fun hasWaitingTimeChanged(): Boolean {
        Log.v(TAG, "hasWaitingTimeChanged")
        return canteen?.let {
            it.waitingTime != binding.edtWaitingTime.text.toString().toInt()
        } ?: false
    }

    private fun writeWaitingTime() = lifecycleScope.launch {
        Log.v(TAG, "writeWaitingTime")
        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().updateWaitingTime(
            token,
            binding.edtWaitingTime.text.toString().toInt()
        )
            .onFailure {
                Log.e(TAG, "Error updating waiting time")
                Toast.makeText(
                    this@OverviewActivity,
                    "Error updating waiting time",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSuccess {
                Log.v(TAG, "Successfully updated waiting time")
                // If you want to reload updated data from server:
                updateCanteenDetails()
            }
    }
}
