package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
import java.util.List;
 
// Response đầy đủ khi đọc 1 chapter
// Bao gồm: nội dung ảnh + điều hướng prev/next
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDetailResponse {
 
    private Long id;
    private Integer chapterNumber;
    private String title;
    private LocalDateTime createdAt;
 
    // Thông tin truyện (để hiển thị breadcrumb trên app)
    private Long storyId;
    private String storyTitle;
 
    // Danh sách ảnh của chapter (đã sắp xếp theo order_index)
    private List<ChapterImageResponse> images;
 
    // Điều hướng đọc tiếp / đọc lại
    private Navigation navigation;
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterImageResponse {
        private Long id;
        private String imageUrl;
        private Integer orderIndex;
    }
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Navigation {
        private Long prevChapterId;     // null nếu đây là chapter đầu
        private Integer prevChapterNumber;
        private Long nextChapterId;     // null nếu đây là chapter cuối
        private Integer nextChapterNumber;
    }
}
