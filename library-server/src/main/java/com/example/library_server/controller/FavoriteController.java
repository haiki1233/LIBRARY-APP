package com.example.library_server.controller;

import com.example.library_server.dto.response.ApiResponse;
import com.example.library_server.dto.response.FavoriteResponse;
import com.example.library_server.dto.response.FavoriteStatusResponse;
import com.example.library_server.dto.response.PageResponse;
import com.example.library_server.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Quản lý truyện yêu thích ❤️")
@SecurityRequirement(name = "bearerAuth")  // Tất cả endpoint đều cần JWT
public class FavoriteController {
 
    private final FavoriteService favoriteService;
 
    // ===== POST /api/favorites/{storyId} =====
    @PostMapping("/{storyId}")
    @Operation(
        summary = "Thêm truyện vào yêu thích",
        description = "Trả về trạng thái isFavorited=true và tổng số lượt yêu thích của truyện"
    )
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID truyện muốn lưu")
            @PathVariable Long storyId) {
 
        FavoriteStatusResponse result = favoriteService.addFavorite(
                userDetails.getUsername(), storyId);
 
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã thêm vào danh sách yêu thích ❤️", result));
    }
 
    // ===== DELETE /api/favorites/{storyId} =====
    @DeleteMapping("/{storyId}")
    @Operation(
        summary = "Bỏ truyện khỏi yêu thích",
        description = "Trả về trạng thái isFavorited=false và tổng số lượt yêu thích còn lại"
    )
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID truyện muốn bỏ")
            @PathVariable Long storyId) {
 
        FavoriteStatusResponse result = favoriteService.removeFavorite(
                userDetails.getUsername(), storyId);
 
        return ResponseEntity.ok(ApiResponse.success("Đã bỏ khỏi danh sách yêu thích", result));
    }
 
    // ===== GET /api/favorites =====
    @GetMapping
    @Operation(
        summary = "Lấy danh sách truyện yêu thích",
        description = "Trả về danh sách truyện đã lưu của user hiện tại, sắp xếp theo thời gian lưu mới nhất"
    )
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getMyFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
 
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
 
            @Parameter(description = "Số truyện mỗi trang")
            @RequestParam(defaultValue = "20") int size) {
 
        PageResponse<FavoriteResponse> result = favoriteService.getMyFavorites(
                userDetails.getUsername(), page, size);
 
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}