package com.example.canteenchecker.adminapp.core

import java.time.LocalDateTime

data class ReviewData(
    val id: String,
    val creationDate: LocalDateTime,
    val creator: String,
    val rating: Int,
    val remark: String
)
