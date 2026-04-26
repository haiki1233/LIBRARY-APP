package com.example.library_server.service;

import com.example.library_server.dto.request.ChangePasswordRequest;
import com.example.library_server.dto.request.UpdateProfileRequest;
import com.example.library_server.dto.response.UserProfileResponse;
import com.example.library_server.entity.User;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
 
    // Thư mục lưu avatar (chỉnh lại nếu dùng S3/Cloudinary)
    private static final String AVATAR_DIR = "uploads/avatars/";
 
    // ===== GET /api/users/me =====
    public UserProfileResponse getMyProfile(String username) {
        User user = findByUsername(username);
        return mapToResponse(user);
    }
 
    // ===== PUT /api/users/me =====
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findByUsername(username);
 
        // Kiểm tra username mới có bị trùng không
        if (request.getUsername() != null
                && !request.getUsername().equals(user.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username '" + request.getUsername() + "' đã được sử dụng", HttpStatus.CONFLICT);
        }
 
        // Kiểm tra email mới có bị trùng không
        if (request.getEmail() != null
                && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email '" + request.getEmail() + "' đã được đăng ký", HttpStatus.CONFLICT);
        }
 
        // Cập nhật thông tin
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
 
        User saved = userRepository.save(user);
        log.info("User {} updated profile", username);
        return mapToResponse(saved);
    }
 
    // ===== POST /api/users/avatar =====
    @Transactional
    public UserProfileResponse uploadAvatar(String username, MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            throw new AppException("File ảnh không được để trống", HttpStatus.BAD_REQUEST);
        }
 
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException("Chỉ chấp nhận file ảnh (jpg, png, webp...)", HttpStatus.BAD_REQUEST);
        }
 
        // Giới hạn 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new AppException("Ảnh không được vượt quá 5MB", HttpStatus.BAD_REQUEST);
        }
 
        User user = findByUsername(username);
 
        try {
            // Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(AVATAR_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
 
            // Tạo tên file unique để tránh trùng
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(fileName);
 
            // Lưu file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
 
            // Xóa avatar cũ nếu có
            if (user.getAvatar() != null) {
                deleteOldAvatar(user.getAvatar());
            }
 
            // Cập nhật DB
            String avatarUrl = "/" + AVATAR_DIR + fileName;
            user.setAvatar(avatarUrl);
            User saved = userRepository.save(user);
 
            log.info("User {} uploaded avatar: {}", username, avatarUrl);
            return mapToResponse(saved);
 
        } catch (IOException e) {
            log.error("Failed to upload avatar for user {}: {}", username, e.getMessage());
            throw new AppException("Lỗi khi upload ảnh, vui lòng thử lại", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
 
    // ===== PUT /api/users/change-password =====
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        // Kiểm tra newPassword == confirmPassword
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException("Mật khẩu xác nhận không khớp", HttpStatus.BAD_REQUEST);
        }
 
        User user = findByUsername(username);
 
        // Kiểm tra mật khẩu cũ có đúng không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException("Mật khẩu cũ không đúng", HttpStatus.UNAUTHORIZED);
        }
 
        // Kiểm tra mật khẩu mới không được trùng mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AppException("Mật khẩu mới không được trùng mật khẩu cũ", HttpStatus.BAD_REQUEST);
        }
 
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed password successfully", username);
    }
 
    // ===== Helper methods =====
 
    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Không tìm thấy user", HttpStatus.NOT_FOUND));
    }
 
    private UserProfileResponse mapToResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
 
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
 
    private void deleteOldAvatar(String avatarUrl) {
        try {
            // Bỏ dấu "/" đầu tiên
            Path oldFile = Paths.get(avatarUrl.startsWith("/") ? avatarUrl.substring(1) : avatarUrl);
            Files.deleteIfExists(oldFile);
        } catch (IOException e) {
            log.warn("Could not delete old avatar: {}", avatarUrl);
        }
    }
}
