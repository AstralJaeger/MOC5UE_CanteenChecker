package com.example.canteenchecker.adminapp.core

data class OwnedCanteenDetails(
    val id: String,
    val name: String,
    val address: String,
    val phoneNumber: String,
    val website: String,
    val dish: String,
    val dishPrice: Double,
    val waitingTime: Int
)