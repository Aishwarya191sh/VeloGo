package com.example.frontend.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("success", alternate = ["succes"])
    val success: Boolean? = null,
    val message: String? = null,
    val data: T? = null
)
