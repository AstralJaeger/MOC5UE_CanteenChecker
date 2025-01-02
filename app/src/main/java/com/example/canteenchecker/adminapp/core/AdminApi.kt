package com.example.canteenchecker.adminapp.core

interface AdminApi {

    suspend fun authenticate(userName: String, password: String): Result<String>

    suspend fun getOwnedCanteenDetails(authenticationToken: String): Result<OwnedCanteenDetails>

    suspend fun getCanteenReviewStatistics(authenticationToken: String): Result<CanteenReviewStatistics>

    suspend fun updateCanteenData(authenticationToken: String, name: String, address: String, website: String, phoneNumber: String): Result<Unit>

    suspend fun updateCanteenDish(authenticationToken: String, dish: String, dishPrice: Double): Result<Unit>

    suspend fun updateWaitingTime(authenticationToken: String, waitingTime: Int): Result<Unit>

    suspend fun getCanteenReviews(authenticationToken: String): Result<List<ReviewData>>

    suspend fun deleteReview(authenticationToken: String, reviewId: String): Result<Unit>
}
