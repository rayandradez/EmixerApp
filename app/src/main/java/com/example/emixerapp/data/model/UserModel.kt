package com.example.emixerapp.data.model

// TODO - Explain the data Class
data class UserModel(
    val name: String = "",
    val iconIndex: Int = 0, // Added icon index
    val bass: Int = 0,
    val middle: Int = 0,
    val truble: Int = 0
)