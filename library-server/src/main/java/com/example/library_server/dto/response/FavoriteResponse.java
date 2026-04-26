package com.example.library_server.dto.response;

import com.example.library_server.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
import java.util.List;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
 
    private Long favoriteId;
    private LocalDateTime savedAt;      // Thời điểm lưu vào danh sách yêu thích
 
    // Thông tin truyện
    private Long storyId;
    private String storyTitle;
    private String storyAuthor;
    private String storyCoverImage;
    private Story.StoryStatus storyStatus;
    private Integer totalChapters;
    private Double avgRating;
    private List<GenreResponse> genres;
    private LocalDateTime storyUpdatedAt;
}