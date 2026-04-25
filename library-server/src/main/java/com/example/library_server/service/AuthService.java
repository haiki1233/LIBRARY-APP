package com.example.library_server.service;

import com.example.library_server.dto.request.LoginRequest;
import com.example.library_server.dto.request.RegisterRequest;
import com.example.library_server.dto.response.AuthResponse;
import com.example.library_server.entity.User;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.UserRepository;
import com.example.library_server.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
 
    /**
     * Đăng ký tài khoản mới
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username '" + request.getUsername() + "' đã được sử dụng", HttpStatus.CONFLICT);
        }
 
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email '" + request.getEmail() + "' đã được đăng ký", HttpStatus.CONFLICT);
        }
 
        // Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa password bằng BCrypt
                .role(User.Role.USER)
                .build();
 
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());
 
        // Tạo JWT tokens
        return buildAuthResponse(savedUser);
    }
 
    /**
     * Đăng nhập - hỗ trợ cả username lẫn email
     */
    public AuthResponse login(LoginRequest request) {
        // Tìm user theo username hoặc email
        User user = userRepository
                .findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại", HttpStatus.UNAUTHORIZED));
 
        // Xác thực password qua Spring Security
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(), // Luôn dùng username để authenticate
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new AppException("Mật khẩu không đúng", HttpStatus.UNAUTHORIZED);
        }
 
        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(user);
    }
 
    /**
     * Build response với access token + refresh token
     */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
 
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .avatar(user.getAvatar())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
