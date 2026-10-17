package com.fei.firstapp

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface FirstappApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @GET("api/me")
    suspend fun me(@Header("Authorization") bearerToken: String): User

    @GET("api/permissions")
    suspend fun permissions(@Header("Authorization") bearerToken: String): PermissionResponse

    @GET("api/vip/site")
    suspend fun vipSite(@Header("Authorization") bearerToken: String): VipSiteResponse

    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") bearerToken: String,
        @Body request: RefreshRequest
    ): ApiMessage
}
