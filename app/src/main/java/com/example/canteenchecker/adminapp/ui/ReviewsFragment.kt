package com.example.canteenchecker.adminapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.CanteenCheckerAdminApplication
import com.example.canteenchecker.adminapp.core.CanteenChangedBroadcastReceiver
import com.example.canteenchecker.adminapp.core.registerCanteenChangedBroadcastReceiver
import com.example.canteenchecker.adminapp.core.unregisterCanteenChangedBroadcastReceiver
import com.example.canteenchecker.adminapp.databinding.FragmentReviewsBinding
import com.example.canteenchecker.consumerapp.api.AdminApiFactory
import kotlinx.coroutines.launch
import java.util.Locale

private const val CANTEEN_ID = "CanteenId"

class ReviewsFragment : Fragment() {

    private lateinit var binding: FragmentReviewsBinding

    private var canteenId: String? = null

    private val receiver = object : CanteenChangedBroadcastReceiver(){
        override fun onReceiveCanteenChanged(canteenId: String) {
            updateReviews()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            canteenId = it.getString(CANTEEN_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireContext().registerCanteenChangedBroadcastReceiver(receiver)
        updateReviews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterCanteenChangedBroadcastReceiver(receiver)
    }

    private fun updateReviews() = lifecycleScope.launch {
        val token = (requireActivity().application as CanteenCheckerAdminApplication).authenticationToken!!
        AdminApiFactory.createApi().getCanteenReviewStatistics(token)
            .onFailure {
                binding.txvAverageRating.text = null
                binding.rtbAverageRating.rating = 0f
                binding.txvTotalRatings.text = null
                binding.prbRatingsOne.max = 1
                binding.prbRatingsOne.progress = 0
                binding.prbRatingsTwo.max = 1
                binding.prbRatingsTwo.progress = 0
                binding.prbRatingsThree.max = 1
                binding.prbRatingsThree.progress = 0
                binding.prbRatingsFour.max = 1
                binding.prbRatingsFour.progress = 0
                binding.prbRatingsFive.max = 1
                binding.prbRatingsFive.progress = 0
            }
            .onSuccess {
                binding.txvAverageRating.text = String.format(Locale.getDefault(), "%.1f", it.averageRating)
                binding.rtbAverageRating.rating = it.averageRating
                binding.txvTotalRatings.text = String.format(Locale.getDefault(), "%d", it.totalRatings)
                binding.prbRatingsOne.max = it.totalRatings
                binding.prbRatingsOne.progress = it.countOneStar
                binding.prbRatingsTwo.max = it.totalRatings
                binding.prbRatingsTwo.progress = it.countTwoStars
                binding.prbRatingsThree.max = it.totalRatings
                binding.prbRatingsThree.progress = it.countThreeStars
                binding.prbRatingsFour.max = it.totalRatings
                binding.prbRatingsFour.progress = it.countFourStars
                binding.prbRatingsFive.max = it.totalRatings
                binding.prbRatingsFive.progress = it.countFiveStars
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(canteenId: String) =
            ReviewsFragment().apply {
                arguments = Bundle().apply {
                    putString(CANTEEN_ID, canteenId)
                }
            }
    }
}