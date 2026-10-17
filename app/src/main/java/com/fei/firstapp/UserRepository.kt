package com.fei.firstapp

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.Proxy

private const val API_BASE_URL = "http://127.0.0.1:8000/"

object ApiClient {
    val api: FirstappApi by lazy {
        val httpClient = OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY)
            .build()

        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirstappApi::class.java)
    }
}

class UserRepository(context: Context) {
    private val api = ApiClient.api
    private val tokenStore = TokenStore(context.applicationContext)
    private val gson = Gson()

    suspend fun register(phone: String, password: String): User = withContext(Dispatchers.IO) {
        val response = api.register(AuthRequest(phone.trim(), password))
        tokenStore.save(response.accessToken, response.refreshToken)
        response.user
    }

    suspend fun login(phone: String, password: String): User = withContext(Dispatchers.IO) {
        val response = api.login(AuthRequest(phone.trim(), password))
        tokenStore.save(response.accessToken, response.refreshToken)
        response.user
    }

    suspend fun restoreSession(): User? = withContext(Dispatchers.IO) {
        val accessToken = tokenStore.accessToken() ?: return@withContext null
        try {
            api.me("Bearer $accessToken")
        } catch (error: HttpException) {
            if (error.code() != 401) throw error
            val refreshToken = tokenStore.refreshToken() ?: run {
                tokenStore.clear()
                return@withContext null
            }
            try {
                val refreshed = api.refresh(RefreshRequest(refreshToken))
                tokenStore.save(refreshed.accessToken, refreshed.refreshToken)
                refreshed.user
            } catch (refreshError: Exception) {
                tokenStore.clear()
                throw refreshError
            }
        }
    }

    suspend fun getPermissions(): PermissionResponse = withContext(Dispatchers.IO) {
        val access = validAccessToken()
        api.permissions("Bearer $access")
    }

    suspend fun getVipSiteUrl(): String = withContext(Dispatchers.IO) {
        val access = validAccessToken()
        api.vipSite("Bearer $access").url
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val refresh = tokenStore.refreshToken()
        val access = tokenStore.accessToken()
        try {
            if (refresh != null && access != null) {
                api.logout("Bearer $access", RefreshRequest(refresh))
            }
        } finally {
            tokenStore.clear()
        }
    }

    private suspend fun validAccessToken(): String {
        val access = tokenStore.accessToken() ?: throw IllegalStateException("请先登录")
        return try {
            // Permission endpoint verifies the token; refresh is handled on the next session restore.
            access
        } catch (error: Exception) {
            throw error
        }
    }

    fun errorMessage(error: Throwable): String {
        Log.e("FirstappApi", "API request failed", error)
        if (error is HttpException) {
            val body = error.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                val detail = runCatching { gson.fromJson(body, ApiError::class.java)?.detail }.getOrNull()
                if (!detail.isNullOrBlank()) return detail
            }
            return when (error.code()) {
                401 -> "账号或密码错误，或登录已过期"
                409 -> "手机号已注册"
                else -> "服务器请求失败（${error.code()}）"
            }
        }
        return when (error) {
            is IOException -> "连接失败：${error.javaClass.simpleName}: ${error.message}"
            else -> error.message ?: "请求失败，请稍后重试"
        }
    }

    private data class ApiError(val detail: String?)
}

private class TokenStore(context: Context) {
    private val preferences = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    fun save(access: String, refresh: String) {
        preferences.edit().putString("access", access).putString("refresh", refresh).apply()
    }

    fun accessToken(): String? = preferences.getString("access", null)
    fun refreshToken(): String? = preferences.getString("refresh", null)
    fun clear() = preferences.edit().clear().apply()
}



