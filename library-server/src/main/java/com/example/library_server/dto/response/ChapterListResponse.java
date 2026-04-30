package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
import java.util.List;
 
// Response danh sách chapter của 1 truyện
// Trả về thêm thông tin truyện để mobile hiển thị header
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListResponse {
 
    // Thông tin truyện (hiển thị header)
    private Long storyId;
    private String storyTitle;
    private String storyCoverImage;
    private long totalChapters;
 
    // Danh sách chapter
    private List<ChapterItemResponse> chapters;
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterItemResponse {
        private Long id;
        private Integer chapterNumber;
        private String title;
        private LocalDateTime createdAt;
    }
}