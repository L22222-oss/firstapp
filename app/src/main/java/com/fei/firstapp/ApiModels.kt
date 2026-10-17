package com.fei.firstapp

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    val phone: String,
    val password: String
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: User
)

data class ApiMessage(val message: String)

data class PermissionResponse(
    val calculator: Boolean,
    val audio: Boolean,
    val video: Boolean,
    val vip: Boolean
)

data class VipSiteResponse(
    val url: String
)

