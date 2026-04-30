package com.example.library_mobile.data.repository

import com.example.library_mobile.data.local.TokenManager
import com.example.library_mobile.data.remote.api.AuthApiService
import com.example.library_mobile.data.remote.dto.AuthResponse
import com.example.library_mobile.data.remote.dto.LoginRequest
import com.example.library_mobile.data.remote.dto.RegisterRequest
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiService: AuthApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        // Lưu token + user info vào local storage
                        tokenManager.saveTokens(
                            accessToken  = body.data.accessToken,
                            refreshToken = body.data.refreshToken
                        )
                        tokenManager.saveUser(
                            userId   = body.data.user.id,
                            username = body.data.user.username,
                            email    = body.data.user.email,
                            avatar   = body.data.user.avatar
                        )
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.message ?: "Đăng nhập thất bại")
                    }
                } else {
                    // Parse error message từ response body
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.Error(errorMsg, response.code())
                }
            } catch (e: Exception) {
                Result.Error(mapNetworkError(e))
            }
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String
    ): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(RegisterRequest(username, email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        tokenManager.saveTokens(
                            accessToken  = body.data.accessToken,
                            refreshToken = body.data.refreshToken
                        )
                        tokenManager.saveUser(
                            userId   = body.data.user.id,
                            username = body.data.user.username,
                            email    = body.data.user.email,
                            avatar   = body.data.user.avatar
                        )
                        Result.Success(body.data)
                    } else {
                        Result.Error(body?.message ?: "Đăng ký thất bại")
                    }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    Result.Error(errorMsg, response.code())
                }
            } catch (e: Exception) {
                Result.Error(mapNetworkError(e))
            }
        }
    }

    // Parse error JSON từ backend: {"success": false, "message": "..."}
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = org.json.JSONObject(errorBody ?: "")
            json.optString("message", "Đã có lỗi xảy ra")
        } catch (e: Exception) {
            "Đã có lỗi xảy ra"
        }
    }

    private fun mapNetworkError(e: Exception): String {
        return when (e) {
            is java.net.UnknownHostException    -> "Không có kết nối mạng"
            is java.net.SocketTimeoutException  -> "Kết nối quá thời gian, thử lại sau"
            is java.io.IOException              -> "Lỗi mạng, vui lòng thử lại"
            else                               -> e.message ?: "Lỗi không xác định"
        }
    }
}