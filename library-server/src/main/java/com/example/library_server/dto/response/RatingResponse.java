package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
 
    // Đánh giá của user hiện tại
    private Long ratingId;
    private Integer myScore;          // Điểm user vừa chấm
    private boolean isNew;            // true = tạo mới, false = cập nhật
 
    // Thống kê tổng hợp của truyện (cập nhật realtime sau khi rate)
    private Long storyId;
    private Double avgScore;          // Điểm trung bình
    private Long totalRatings;        // Tổng số lượt đánh giá
    private ScoreDistribution distribution; // Phân bố điểm 1-5 sao
 
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDistribution {
        private long oneStar;
        private long twoStar;
        private long threeStar;
        private long fourStar;
        private long fiveStar;
    }
}
