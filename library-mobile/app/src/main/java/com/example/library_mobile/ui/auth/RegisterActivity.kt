package com.example.library_mobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.library_mobile.R
import com.example.library_mobile.databinding.ActivityRegisterBinding
import com.example.library_mobile.di.Injection
import com.example.library_mobile.utils.Result

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(Injection.provideAuthRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Nút Đăng ký
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            binding.tvError.visibility = View.GONE
            viewModel.register(username, email, password, confirmPassword)
        }

        // Chuyển sang Đăng nhập
        binding.tvLogin.setOnClickListener {
            finish() // Quay lại LoginActivity nếu nó đã ở trong stack
        }

        // Theo dõi độ mạnh mật khẩu khi nhập
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.checkPasswordStrength(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Xóa lỗi khi focus vào các trường
        binding.etUsername.setOnFocusChangeListener { _, _ -> viewModel.clearUsernameError() }
        binding.etEmail.setOnFocusChangeListener { _, _ -> viewModel.clearEmailError() }
        binding.etPassword.setOnFocusChangeListener { _, _ -> viewModel.clearPasswordError() }
        binding.etConfirmPassword.setOnFocusChangeListener { _, _ -> viewModel.clearConfirmPasswordError() }
    }

    private fun observeViewModel() {
        // Theo dõi kết quả đăng ký
        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(this, com.example.library_mobile.ui.home.HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    showLoading(false)
                    binding.tvError.text = result.message
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }

        // Theo dõi lỗi các trường nhập liệu
        viewModel.usernameError.observe(this) { error -> binding.tilUsername.error = error }
        viewModel.emailError.observe(this) { error -> binding.tilEmail.error = error }
        viewModel.passwordError.observe(this) { error -> binding.tilPassword.error = error }
        viewModel.confirmPasswordError.observe(this) { error -> binding.tilConfirmPassword.error = error }

        // Theo dõi độ mạnh mật khẩu
        viewModel.passwordStrength.observe(this) { strength ->
            updatePasswordStrengthUI(strength)
        }
    }

    private fun updatePasswordStrengthUI(strength: AuthViewModel.PasswordStrength) {
        if (strength == AuthViewModel.PasswordStrength.NONE) {
            binding.layoutPasswordStrength.visibility = View.GONE
            return
        }

        binding.layoutPasswordStrength.visibility = View.VISIBLE
        val context = this
        
        when (strength) {
            AuthViewModel.PasswordStrength.WEAK -> {
                binding.tvStrengthLabel.text = getString(R.string.password_strength_weak)
                binding.tvStrengthLabel.setTextColor(ContextCompat.getColor(context, R.color.error))
                binding.strengthBar1.backgroundTintList = ContextCompat.getColorStateList(context, R.color.error)
                binding.strengthBar2.backgroundTintList = ContextCompat.getColorStateList(context, R.color.divider)
                binding.strengthBar3.backgroundTintList = ContextCompat.getColorStateList(context, R.color.divider)
            }
            AuthViewModel.PasswordStrength.MEDIUM -> {
                binding.tvStrengthLabel.text = getString(R.string.password_strength_medium)
                binding.tvStrengthLabel.setTextColor(ContextCompat.getColor(context, R.color.warning))
                binding.strengthBar1.backgroundTintList = ContextCompat.getColorStateList(context, R.color.warning)
                binding.strengthBar2.backgroundTintList = ContextCompat.getColorStateList(context, R.color.warning)
                binding.strengthBar3.backgroundTintList = ContextCompat.getColorStateList(context, R.color.divider)
            }
            AuthViewModel.PasswordStrength.STRONG -> {
                binding.tvStrengthLabel.text = getString(R.string.password_strength_strong)
                binding.tvStrengthLabel.setTextColor(ContextCompat.getColor(context, R.color.success))
                binding.strengthBar1.backgroundTintList = ContextCompat.getColorStateList(context, R.color.success)
                binding.strengthBar2.backgroundTintList = ContextCompat.getColorStateList(context, R.color.success)
                binding.strengthBar3.backgroundTintList = ContextCompat.getColorStateList(context, R.color.success)
            }
            else -> {}
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnRegister.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        binding.btnRegister.isEnabled = !isLoading
    }
}
