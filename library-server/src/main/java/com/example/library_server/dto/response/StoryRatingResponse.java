package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryRatingResponse {
 
    private Long storyId;
    private String storyTitle;
 
    // Thống kê tổng hợp
    private Double avgScore;          // Điểm trung bình (làm tròn 1 chữ số thập phân)
    private Long totalRatings;        // Tổng lượt đánh giá
 
    // Phân bố điểm 1-5 sao (dùng để vẽ progress bar trên app)
    private RatingResponse.ScoreDistribution distribution;
 
    // Điểm của user hiện tại (null nếu chưa đánh giá)
    private Integer myScore;
}
