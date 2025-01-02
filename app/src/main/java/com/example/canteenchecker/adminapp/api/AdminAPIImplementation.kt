package com.example.canteenchecker.consumerapp.api

import android.util.Log
import com.example.canteenchecker.adminapp.core.AdminApi
import com.example.canteenchecker.adminapp.core.CanteenReviewStatistics
import com.example.canteenchecker.adminapp.core.OwnedCanteenDetails
import com.example.canteenchecker.adminapp.core.ReviewData
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.time.LocalDateTime

object AdminApiFactory {
    fun createApi(): AdminApi =
        AdminApiImplementation("https://moc5.projekte.fh-hagenberg.at/CanteenChecker/api/")
}

// proxy implementation for API version 1.1
// https://moc5.projekte.fh-hagenberg.at/CanteenChecker/swagger/index.html
private class AdminApiImplementation(apiBaseUrl: String) : AdminApi {

    private val TAG: String = this::class.java.canonicalName!!

    private val retrofit = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override suspend fun authenticate(userName: String, password: String): Result<String> =
        apiCall {
            postAuthenticate(userName, password)
        }

    override suspend fun getOwnedCanteenDetails(authenticationToken: String): Result<OwnedCanteenDetails> = apiCall {
        getOwnedCanteenDetails("Bearer $authenticationToken")
    }.convert {
        OwnedCanteenDetails(
            id,
            name,
            address,
            phoneNumber,
            website,
            dish,
            dishPrice,
            waitingTime
        )
    }

    override suspend fun getCanteenReviewStatistics(authenticationToken: String): Result<CanteenReviewStatistics> = apiCall {
        getCanteenReviewStatistics("Bearer $authenticationToken")
    }.convert {
        CanteenReviewStatistics(
            countOneStar,
            countTwoStars,
            countThreeStars,
            countFourStars,
            countFiveStars
        )
    }

    override suspend fun updateCanteenData(
        authenticationToken: String,
        name: String,
        address: String,
        website: String,
        phoneNumber: String
    ) =
        apiCall {
            putCanteenData("Bearer $authenticationToken", name, address, website, phoneNumber)
        }.convert { }

    override suspend fun updateCanteenDish(authenticationToken: String, dish: String, dishPrice: Double) =
        apiCall {
            putCanteenDish("Bearer $authenticationToken", dish, dishPrice)
        }.convert { }

    override suspend fun updateWaitingTime(authenticationToken: String, waitingTime: Int) =
        apiCall {
            putWaitingTime("Bearer $authenticationToken", waitingTime)
        }.convert { }

    override suspend fun getCanteenReviews(authenticationToken: String): Result<List<ReviewData>> =
        apiCall {
            getCanteenReviews("Bearer $authenticationToken")
        }.convertEach {
            ReviewData(id, LocalDateTime.parse(creationDate), creator, rating, remark)
        }

    override suspend fun deleteReview(authenticationToken: String, reviewId: String): Result<Unit> =
        apiCall {
            deleteReview("Bearer $authenticationToken", reviewId)
        }

    private interface Api {
        @POST("admin/authenticate")
        suspend fun postAuthenticate(
            @Query("userName") userName: String, @Query("password") password: String
        ): String

        @GET("admin/canteen")
        suspend fun getOwnedCanteenDetails(@Header("Authorization") authenticationToken: String): ApiOwnedCanteenDetails

        @GET("admin/canteen/review-statistics")
        suspend fun getCanteenReviewStatistics(@Header("Authorization") authenticationToken: String): ApiCanteenReviewStatistics

        @PUT("admin/canteen/data")
        suspend fun putCanteenData(
            @Header("Authorization") authenticationToken: String,
            @Query("name") name: String,
            @Query("address") address: String,
            @Query("website") website: String,
            @Query("phoneNumber") phoneNumber: String
        ): Response<Unit>

        @PUT("admin/canteen/dish")
        suspend fun putCanteenDish(
            @Header("Authorization") authenticationToken: String,
            @Query("dish") dish: String,
            @Query("dishPrice") dishPrice: Double
        ): Response<Unit>

        @PUT("admin/canteen/waiting-time")
        suspend fun putWaitingTime(
            @Header("Authorization") authenticationToken: String,
            @Query("waitingTime") waitingTime: Int
        ): Response<Unit>

        @GET("admin/canteen/reviews")
        suspend fun getCanteenReviews(@Header("Authorization") authenticationToken: String): List<ApiReviewData>

        @DELETE("admin/canteen/reviews/{reviewId}")
        suspend fun deleteReview(
            @Header("Authorization") authenticationToken: String,
            @Path("reviewId") reviewId: String
        ): Response<Unit>
    }

    private class ApiOwnedCanteenDetails(
        val id: String,
        val name: String,
        val address: String,
        val phoneNumber: String,
        val website: String,
        val dish: String,
        val dishPrice: Double,
        val waitingTime: Int
    )

    private class ApiCanteenReviewStatistics(
        val countOneStar: Int,
        val countTwoStars: Int,
        val countThreeStars: Int,
        val countFourStars: Int,
        val countFiveStars: Int
    )

    private class ApiReviewData(
        val id: String,
        val creationDate: String,
        val creator: String,
        val rating: Int,
        val remark: String
    )

    private inline fun <T> apiCall(call: Api.() -> T): Result<T> = try {
        Result.success(call(retrofit.create()))
    } catch (ex: HttpException) {
        Result.failure(ex)
    } catch (ex: IOException) {
        Result.failure(ex)
    }.onFailure {
        Log.e(TAG, "API call failed", it)
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}

private inline fun <T, R> Result<List<T>>.convertEach(map: T.() -> R): Result<List<R>> =
    this.map { it.map(map) }

private inline fun <T, R> Result<T>.convert(map: T.() -> R): Result<R> = this.map(map)