package com.example.library_server.controller;

import com.example.library_server.dto.request.CommentRequest;
import com.example.library_server.dto.response.ApiResponse;
import com.example.library_server.dto.response.CommentResponse;
import com.example.library_server.dto.response.PageResponse;
import com.example.library_server.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Bình luận truyện 💬")
public class CommentController {

    private final CommentService commentService;

    // ===== GET /api/comments/{storyId} =====
    // Không cần đăng nhập
    @GetMapping("/{storyId}")
    @Operation(
        summary = "Lấy danh sách bình luận của truyện",
        description = """
            Trả về comment gốc có phân trang, mỗi comment kèm theo replies.
            - Không cần đăng nhập
            - Sắp xếp: comment mới nhất lên đầu
            - Reply sắp xếp: cũ nhất lên đầu (theo thứ tự thời gian)
            - Comment đã xóa vẫn hiển thị node nhưng content = "[Bình luận đã bị xóa]"
            """
    )
    public ResponseEntity<ApiResponse<PageResponse<CommentResponse>>> getComments(
            @Parameter(description = "ID của truyện")
            @PathVariable Long storyId,

            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số comment mỗi trang")
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<CommentResponse> result = commentService.getCommentsByStory(
                storyId, page, size);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ===== POST /api/comments =====
    // Yêu cầu đăng nhập
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Đăng bình luận mới",
        description = """
            Đăng comment hoặc reply cho 1 truyện.
            - Yêu cầu JWT token
            - Nếu có parentId → là reply cho comment đó
            - Reply chỉ hỗ trợ 1 cấp: reply của reply sẽ tự gắn vào comment gốc
            """
    )
    public ResponseEntity<ApiResponse<CommentResponse>> postComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CommentRequest request) {

        CommentResponse result = commentService.postComment(
                userDetails.getUsername(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bình luận thành công!", result));
    }
}