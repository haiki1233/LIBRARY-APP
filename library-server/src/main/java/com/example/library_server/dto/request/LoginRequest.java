package com.example.library_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
 
@Data
public class LoginRequest {
 
    @NotBlank(message = "Username/Email không được để trống")
    private String username; // Có thể nhập username hoặc email
 
    @NotBlank(message = "Password không được để trống")
    private String password;
}
