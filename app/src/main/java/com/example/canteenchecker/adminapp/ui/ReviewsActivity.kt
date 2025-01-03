package com.example.canteenchecker.adminapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.databinding.ActivityReviewsBinding

class ReviewsActivity: AppCompatActivity() {

    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityReviewsBinding
    private var canteenId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        canteenId = intent.getStringExtra(EXTRA_CANTEEN_ID)
        if (canteenId == null) {
            finish()
            return
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.v(TAG, "onCreateOptionsMenu")
        menuInflater.inflate(R.menu.reviews_menu, menu)
        menu.findItem(R.id.action_back)?.setOnMenuItemClickListener {
            Log.v(TAG, "onBack")
            finish()
            true
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.v(TAG, "onPrepareOptionsMenu")
        menu?.findItem(R.id.action_back)?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.v(TAG, "onOptionsItemSelected")
        return super.onOptionsItemSelected(item)
    }



    companion object {
        private const val EXTRA_CANTEEN_ID = "CANTEEN_ID"

        fun intent(context: Context, canteenId: String): Intent {
            return Intent(context, ReviewsActivity::class.java).apply {
                putExtra(EXTRA_CANTEEN_ID, canteenId)
            }
        }
    }
}