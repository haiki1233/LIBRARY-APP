package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
 
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String role;
    private LocalDateTime createdAt;
}
