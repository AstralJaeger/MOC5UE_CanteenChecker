package com.example.canteenchecker.adminapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.core.CanteenChangedBroadcastReceiver
import com.example.canteenchecker.adminapp.core.ReviewData
import com.example.canteenchecker.adminapp.core.registerCanteenChangedBroadcastReceiver
import com.example.canteenchecker.adminapp.core.sendCanteenChangedBroadcast
import com.example.canteenchecker.adminapp.core.unregisterCanteenChangedBroadcastReceiver
import com.example.canteenchecker.adminapp.databinding.ActivityReviewsBinding
import com.example.canteenchecker.consumerapp.api.AdminApiFactory
import kotlinx.coroutines.launch

// CanteenID Extra
private const val CANTEEN_ID_EXTRA = "canteenId"

class ReviewsActivity: AppCompatActivity() {

    private val TAG = this.javaClass.canonicalName
    private lateinit var binding: ActivityReviewsBinding
    private val reviewsAdapter = ReviewsAdapter { reviewId: String ->
        Log.v(TAG, "onDeleteReview $reviewId")

        AlertDialog.Builder(this)
            .setTitle("Delete Review")
            .setMessage("Do you really want to delete this review?")
            .setPositiveButton("Yes") { _, _ ->
                deleteReview(reviewId)
            }
            .setNegativeButton("No") { _, _ -> }
            .show()
    }

    private val receiver = object : CanteenChangedBroadcastReceiver() {
        override fun onReceiveCanteenChanged(id: String) {
            Log.v(TAG, "onReceiveCanteenChanged")
            updateReviews()
        }
    }

    private val canteenId get() = intent.getStringExtra(CANTEEN_ID_EXTRA) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewsBinding.inflate(layoutInflater)
        setSupportActionBar(binding.toolbar)

        binding.rcyView.adapter = reviewsAdapter
        setContentView(binding.root)

        registerCanteenChangedBroadcastReceiver(receiver)

        binding.srlSwipeRefreshLayout.setOnRefreshListener {
            updateReviews()
            binding.srlSwipeRefreshLayout.isRefreshing = false
        }

        updateReviews()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterCanteenChangedBroadcastReceiver(receiver)
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

    private fun updateReviews() = lifecycleScope.launch {
        Log.v(TAG, "updateReviews")
        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().getCanteenReviews(token)
            .onFailure {
                Toast.makeText(this@ReviewsActivity, "Loading of reviews not successful - please try again", Toast.LENGTH_SHORT).show()
            }.onSuccess { reviews: List<ReviewData> ->
                reviewsAdapter.displayReviews(reviews)
            }
    }

    private fun deleteReview(reviewId: String) = lifecycleScope.launch {
        Log.v(TAG, "deleteReview")
        val token = (application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().deleteReview(token, reviewId)
            .onFailure {
                Toast.makeText(this@ReviewsActivity, "Deletion of review not successful - please try again", Toast.LENGTH_SHORT).show()
            }.onSuccess {
                sendCanteenChangedBroadcast(canteenId)
            }
    }

    private class ReviewsAdapter(private val onDeleteReview: (String) -> Unit): RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

        private val TAG = this.javaClass.canonicalName
        private var reviews = emptyList<ReviewData>()

        private class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val rbRating: RatingBar = itemView.findViewById(R.id.rbRating)
            val tvRemark: TextView = itemView.findViewById(R.id.tvRemark)
            val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthorName)
            val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val review = reviews[position]
            holder.rbRating.rating = review.rating.toFloat()
            holder.tvRemark.text = review.remark
            holder.tvAuthor.text = review.creator
            holder.btnDelete.setOnClickListener {
                Log.v(TAG, "onBtnDeleteClick for ${review.id}")
                onDeleteReview(review.id)
            }
        }

        override fun getItemCount(): Int = reviews.size

        fun displayReviews(reviews: List<ReviewData>) {
            Log.v(TAG, "displayReviews")
            this.reviews = reviews
            notifyDataSetChanged()
        }
    }

    companion object {
        fun intent(context: Context, canteenId: String): Intent {
            return Intent(context, ReviewsActivity::class.java).apply {
                putExtra(CANTEEN_ID_EXTRA, canteenId)
            }
        }
    }
}