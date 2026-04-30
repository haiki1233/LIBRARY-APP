package com.example.library_server.dto.response;

import com.example.library_server.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
import java.util.List;
 
// Response gọn - dùng cho danh sách truyện (list, search, genre filter)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryCardResponse {
 
    private Long id;
    private String title;
    private String author;
    private String coverImage;
    private Story.StoryStatus status;
    private Long viewCount;
    private Double avgRating;       // Điểm trung bình
    private Integer totalChapters;  // Tổng số chapter
    private List<GenreResponse> genres;
    private LocalDateTime updatedAt;
}