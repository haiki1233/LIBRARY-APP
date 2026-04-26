package com.example.library_server.dto.response;

import com.example.library_server.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
import java.util.List;
 
// Response đầy đủ - dùng cho trang chi tiết truyện
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryDetailResponse {
 
    private Long id;
    private String title;
    private String author;
    private String description;
    private String coverImage;
    private Story.StoryStatus status;
    private Long viewCount;
    private Double avgRating;
    private Integer totalRatings;
    private Integer totalChapters;
    private List<GenreResponse> genres;
    private List<ChapterSummaryResponse> chapters; // Danh sách chapter (không có ảnh)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
 
    // Chapter rút gọn - chỉ cần id, số chapter, tiêu đề
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterSummaryResponse {
        private Long id;
        private Integer chapterNumber;
        private String title;
        private LocalDateTime createdAt;
    }
}
