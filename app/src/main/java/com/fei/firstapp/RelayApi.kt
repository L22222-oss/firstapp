package com.fei.firstapp
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
interface RelayApi { @POST("api/user/login") suspend fun login(@Body body: JsonObject): JsonObject; @GET("api/user/self") suspend fun self(@Header("Authorization") authorization:String): JsonObject; @GET("api/log/self") suspend fun logs(@Header("Authorization") authorization:String,@Query("page") page:Int=1,@Query("page_size") size:Int=50):JsonObject }
