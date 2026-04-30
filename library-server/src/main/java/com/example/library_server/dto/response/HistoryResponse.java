package com.example.library_server.dto.response;

import com.example.library_server.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
 
    private Long historyId;
    private LocalDateTime lastReadAt;
    private Integer scrollPosition;     // Vị trí đọc đến (%)
 
    // Thông tin chapter đang đọc
    private Long chapterId;
    private Integer chapterNumber;
    private String chapterTitle;
 
    // Thông tin truyện (để hiển thị card lịch sử)
    private Long storyId;
    private String storyTitle;
    private String storyAuthor;
    private String storyCoverImage;
    private Story.StoryStatus storyStatus;
    private Integer totalChapters;      // Tổng chapter của truyện
}
