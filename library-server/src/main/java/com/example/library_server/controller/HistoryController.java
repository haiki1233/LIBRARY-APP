package com.example.library_server.controller;

import com.example.library_server.dto.request.SaveHistoryRequest;
import com.example.library_server.dto.response.ApiResponse;
import com.example.library_server.dto.response.HistoryResponse;
import com.example.library_server.dto.response.PageResponse;
import com.example.library_server.service.HistoryService;
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
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "Reading History", description = "Lịch sử đọc truyện 📖")
@SecurityRequirement(name = "bearerAuth")
public class HistoryController {
 
    private final HistoryService historyService;
 
    // ===== POST /api/history =====
    @PostMapping
    @Operation(
        summary = "Lưu lịch sử đọc",
        description = """
            Gọi mỗi khi user mở đọc 1 chapter hoặc cuộn đến vị trí mới.
            - Nếu chưa đọc chapter này → tạo mới
            - Nếu đã đọc rồi → cập nhật lastReadAt và scrollPosition
            
            scrollPosition: vị trí cuộn trang (0-100%)
            """
    )
    public ResponseEntity<ApiResponse<HistoryResponse>> saveHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SaveHistoryRequest request) {
 
        HistoryResponse result = historyService.saveHistory(
                userDetails.getUsername(), request);
 
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã lưu lịch sử đọc", result));
    }
 
    // ===== GET /api/history =====
    @GetMapping
    @Operation(
        summary = "Lấy lịch sử đọc",
        description = "Trả về danh sách truyện đã đọc, sắp xếp theo thời gian đọc gần nhất"
    )
    public ResponseEntity<ApiResponse<PageResponse<HistoryResponse>>> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails,
 
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
 
            @Parameter(description = "Số item mỗi trang")
            @RequestParam(defaultValue = "20") int size) {
 
        PageResponse<HistoryResponse> result = historyService.getMyHistory(
                userDetails.getUsername(), page, size);
 
        return ResponseEntity.ok(ApiResponse.success(result));
    }
 
    // ===== DELETE /api/history/{id} =====
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Xóa 1 mục lịch sử",
        description = "Chỉ xóa được lịch sử của chính mình. " +
                      "Trả về 404 nếu không tìm thấy hoặc không có quyền."
    )
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "ID của record lịch sử")
            @PathVariable Long id) {
 
        historyService.deleteHistory(userDetails.getUsername(), id);
 
        return ResponseEntity.ok(ApiResponse.success("Đã xóa khỏi lịch sử", null));
    }
}
