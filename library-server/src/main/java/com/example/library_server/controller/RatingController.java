package com.example.library_server.controller;

import com.example.library_server.dto.request.RatingRequest;
import com.example.library_server.dto.response.ApiResponse;
import com.example.library_server.dto.response.RatingResponse;
import com.example.library_server.dto.response.StoryRatingResponse;
import com.example.library_server.service.RatingService;
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
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Đánh giá truyện ⭐")
public class RatingController {
 
    private final RatingService ratingService;
 
    // ===== POST /api/ratings =====
    // Yêu cầu đăng nhập
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Đánh giá truyện (1-5 sao)",
        description = """
            Tạo mới hoặc cập nhật đánh giá của user cho 1 truyện.
            - Mỗi user chỉ được đánh giá 1 lần / 1 truyện
            - Gọi lại với score khác → tự động cập nhật
            - Trả về thống kê phân bố điểm để vẽ biểu đồ sao ngay lập tức
            """
    )
    public ResponseEntity<ApiResponse<RatingResponse>> rateStory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RatingRequest request) {
 
        RatingResponse result = ratingService.rateStory(
                userDetails.getUsername(), request);
 
        String message = result.isNew()
                ? "Cảm ơn bạn đã đánh giá! ⭐".repeat(result.getMyScore())
                : "Đã cập nhật đánh giá của bạn";
 
        return ResponseEntity
                .status(result.isNew() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(ApiResponse.success(message, result));
    }
 
    // ===== GET /api/ratings/{storyId} =====
    // Không bắt buộc đăng nhập - nhưng nếu có token thì trả thêm myScore
    @GetMapping("/{storyId}")
    @Operation(
        summary = "Lấy thống kê đánh giá của truyện",
        description = """
            Trả về điểm trung bình, tổng lượt đánh giá và phân bố 1-5 sao.
            - Không cần đăng nhập để xem thống kê
            - Nếu đính kèm JWT token → trả thêm myScore (điểm của user hiện tại)
            - myScore = null nếu user chưa đánh giá
            """
    )
    public ResponseEntity<ApiResponse<StoryRatingResponse>> getStoryRating(
            @Parameter(description = "ID của truyện")
            @PathVariable Long storyId,
 
            // Optional - không bắt buộc có token
            @AuthenticationPrincipal UserDetails userDetails) {
 
        String username = (userDetails != null) ? userDetails.getUsername() : null;
        StoryRatingResponse result = ratingService.getStoryRating(storyId, username);
 
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
