package com.example.library_server.controller;

import com.example.library_server.dto.response.ApiResponse;
import com.example.library_server.dto.response.PageResponse;
import com.example.library_server.dto.response.StoryCardResponse;
import com.example.library_server.dto.response.StoryDetailResponse;
import com.example.library_server.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
@Tag(name = "Stories", description = "API truyện - không cần đăng nhập")
public class StoryController {

    private final StoryService storyService;

    // ===== GET /api/stories =====
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả truyện",
               description = "Hỗ trợ phân trang và sắp xếp theo: updatedAt, viewCount, title, createdAt")
    public ResponseEntity<ApiResponse<PageResponse<StoryCardResponse>>> getAllStories(
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số truyện mỗi trang")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sắp xếp theo: updatedAt | viewCount | title | createdAt")
            @RequestParam(defaultValue = "updatedAt") String sortBy) {

        PageResponse<StoryCardResponse> result = storyService.getAllStories(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ===== GET /api/stories/{id} =====
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết truyện theo ID",
               description = "Trả về đầy đủ thông tin + danh sách chapter. Mỗi lần gọi tăng 1 view.")
    public ResponseEntity<ApiResponse<StoryDetailResponse>> getStoryById(
            @PathVariable Long id) {

        StoryDetailResponse result = storyService.getStoryById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ===== GET /api/stories/search?q=... =====
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm truyện",
               description = "Tìm theo tên truyện hoặc tên tác giả, không phân biệt hoa thường")
    public ResponseEntity<ApiResponse<PageResponse<StoryCardResponse>>> searchStories(
            @Parameter(description = "Từ khóa tìm kiếm")
            @RequestParam(name = "q", required = false, defaultValue = "") String keyword,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<StoryCardResponse> result = storyService.searchStories(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ===== GET /api/stories/genre/{id} =====
    @GetMapping("/genre/{genreId}")
    @Operation(summary = "Lọc truyện theo thể loại")
    public ResponseEntity<ApiResponse<PageResponse<StoryCardResponse>>> getStoriesByGenre(
            @PathVariable Long genreId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<StoryCardResponse> result = storyService.getStoriesByGenre(genreId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
