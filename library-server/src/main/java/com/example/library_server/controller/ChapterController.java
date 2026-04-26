package com.example.library_server.controller;

import com.example.library_server.dto.response.ApiResponse;
import com.example.library_server.dto.response.ChapterDetailResponse;
import com.example.library_server.dto.response.ChapterListResponse;
import com.example.library_server.service.ChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequiredArgsConstructor
@Tag(name = "Chapters", description = "API chapter - không cần đăng nhập")
public class ChapterController {
 
    private final ChapterService chapterService;
 
    // ===== GET /api/chapters/{storyId} =====
    @GetMapping("/api/chapters/{storyId}")
    @Operation(
        summary = "Lấy danh sách chapter của truyện",
        description = "Trả về toàn bộ chapter của 1 truyện, sắp xếp từ chapter 1 trở đi. " +
                      "Kèm theo tên truyện và cover để hiển thị header trên app."
    )
    public ResponseEntity<ApiResponse<ChapterListResponse>> getChaptersByStory(
            @Parameter(description = "ID của truyện")
            @PathVariable Long storyId) {
 
        ChapterListResponse result = chapterService.getChaptersByStory(storyId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
 
    // ===== GET /api/chapter/{id} =====
    @GetMapping("/api/chapter/{id}")
    @Operation(
        summary = "Đọc chi tiết 1 chapter",
        description = "Trả về danh sách ảnh (đã sắp xếp theo thứ tự) + điều hướng prev/next chapter. " +
                      "Nếu là chapter đầu thì prevChapterId = null, chapter cuối thì nextChapterId = null."
    )
    public ResponseEntity<ApiResponse<ChapterDetailResponse>> getChapterDetail(
            @Parameter(description = "ID của chapter")
            @PathVariable Long id) {
 
        ChapterDetailResponse result = chapterService.getChapterDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}