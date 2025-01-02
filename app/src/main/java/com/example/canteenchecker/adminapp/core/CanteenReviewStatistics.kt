package com.example.canteenchecker.adminapp.core

data class CanteenReviewStatistics(
    val countOneStar: Int,
    val countTwoStars: Int,
    val countThreeStars: Int,
    val countFourStars: Int,
    val countFiveStars: Int
) {
    val totalRatings = countOneStar + countTwoStars + countThreeStars + countFourStars + countFiveStars;
    val averageRating = if (totalRatings == 0) 0f else countOneStar + (countTwoStars * 2) + (countThreeStars * 3) + (countFourStars * 4) + (countFiveStars * 5) / totalRatings.toFloat()
}
