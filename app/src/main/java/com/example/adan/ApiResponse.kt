package com.example.adan

data class ApiResponse(
    val code: Int,
    val `data`: Data?,
    val status: String
)