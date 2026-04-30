package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;           // "[Đã xóa]" nếu isDeleted = true
    private boolean isDeleted;
    private boolean isEdited;         // true nếu updatedAt > createdAt
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin người bình luận
    private AuthorInfo author;

    // Replies (chỉ load 1 cấp - không đệ quy vô hạn)
    private List<CommentResponse> replies;
    private int replyCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String avatar;
    }
}