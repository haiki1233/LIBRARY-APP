package com.example.library_mobile.ui.auth

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.library_mobile.data.remote.dto.AuthResponse
import com.example.library_mobile.data.repository.AuthRepository
import com.example.library_mobile.utils.Result
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    // ===== LOGIN STATE =====
    private val _loginResult = MutableLiveData<Result<AuthResponse>>()
    val loginResult: LiveData<Result<AuthResponse>> = _loginResult

    // ===== REGISTER STATE =====
    private val _registerResult = MutableLiveData<Result<AuthResponse>>()
    val registerResult: LiveData<Result<AuthResponse>> = _registerResult

    // ===== FIELD ERRORS =====
    private val _usernameError       = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError

    private val _emailError          = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError       = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError: LiveData<String?> = _confirmPasswordError

    // ===== PASSWORD STRENGTH =====
    private val _passwordStrength = MutableLiveData<PasswordStrength>()
    val passwordStrength: LiveData<PasswordStrength> = _passwordStrength

    // ===== LOGIN =====
    fun login(username: String, password: String) {
        if (!validateLogin(username, password)) return

        viewModelScope.launch {
            _loginResult.value = Result.Loading
            _loginResult.value = repository.login(username.trim(), password)
        }
    }

    private fun validateLogin(username: String, password: String): Boolean {
        var isValid = true

        if (username.isBlank()) {
            _usernameError.value = "Vui lòng nhập username hoặc email"
            isValid = false
        } else {
            _usernameError.value = null
        }

        if (password.isBlank()) {
            _passwordError.value = "Vui lòng nhập mật khẩu"
            isValid = false
        } else if (password.length < 6) {
            _passwordError.value = "Mật khẩu phải ít nhất 6 ký tự"
            isValid = false
        } else {
            _passwordError.value = null
        }

        return isValid
    }

    // ===== REGISTER =====
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (!validateRegister(username, email, password, confirmPassword)) return

        viewModelScope.launch {
            _registerResult.value = Result.Loading
            _registerResult.value = repository.register(username.trim(), email.trim(), password)
        }
    }

    private fun validateRegister(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        // Username
        when {
            username.isBlank() -> {
                _usernameError.value = "Vui lòng nhập username"
                isValid = false
            }
            username.trim().length < 3 -> {
                _usernameError.value = "Username phải ít nhất 3 ký tự"
                isValid = false
            }
            else -> _usernameError.value = null
        }

        // Email
        when {
            email.isBlank() -> {
                _emailError.value = "Vui lòng nhập email"
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                _emailError.value = "Email không hợp lệ"
                isValid = false
            }
            else -> _emailError.value = null
        }

        // Password
        when {
            password.isBlank() -> {
                _passwordError.value = "Vui lòng nhập mật khẩu"
                isValid = false
            }
            password.length < 6 -> {
                _passwordError.value = "Mật khẩu phải ít nhất 6 ký tự"
                isValid = false
            }
            else -> _passwordError.value = null
        }

        // Confirm Password
        when {
            confirmPassword.isBlank() -> {
                _confirmPasswordError.value = "Vui lòng xác nhận mật khẩu"
                isValid = false
            }
            password != confirmPassword -> {
                _confirmPasswordError.value = "Mật khẩu xác nhận không khớp"
                isValid = false
            }
            else -> _confirmPasswordError.value = null
        }

        return isValid
    }

    // ===== PASSWORD STRENGTH =====
    fun checkPasswordStrength(password: String) {
        if (password.isEmpty()) {
            _passwordStrength.value = PasswordStrength.NONE
            return
        }

        val strength = when {
            password.length >= 8
                    && password.any { it.isUpperCase() }
                    && password.any { it.isDigit() }
                    && password.any { !it.isLetterOrDigit() } -> PasswordStrength.STRONG

            password.length >= 6
                    && (password.any { it.isUpperCase() }
                    || password.any { it.isDigit() }) -> PasswordStrength.MEDIUM

            else -> PasswordStrength.WEAK
        }
        _passwordStrength.value = strength
    }

    // Clear errors khi user bắt đầu nhập lại
    fun clearUsernameError()       { _usernameError.value = null }
    fun clearEmailError()          { _emailError.value = null }
    fun clearPasswordError()       { _passwordError.value = null }
    fun clearConfirmPasswordError(){ _confirmPasswordError.value = null }

    enum class PasswordStrength { NONE, WEAK, MEDIUM, STRONG }
}