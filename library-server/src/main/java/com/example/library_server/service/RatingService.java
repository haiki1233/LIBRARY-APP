package com.example.library_server.service;

import com.example.library_server.dto.request.RatingRequest;
import com.example.library_server.dto.response.RatingResponse;
import com.example.library_server.dto.response.StoryRatingResponse;
import com.example.library_server.entity.Rating;
import com.example.library_server.entity.Story;
import com.example.library_server.entity.User;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.RatingRepository;
import com.example.library_server.repository.StoryRepository;
import com.example.library_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {
 
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
 
    // ===== POST /api/ratings =====
    // Tạo mới hoặc cập nhật rating (UPSERT)
    @Transactional
    public RatingResponse rateStory(String username, RatingRequest request) {
        User user = getUser(username);
        Story story = getStory(request.getStoryId());
 
        // Thử UPDATE trước
        int updated = ratingRepository.updateScore(
                user.getId(), story.getId(), request.getScore());
 
        Rating rating;
        boolean isNew = (updated == 0);
 
        if (isNew) {
            // Chưa có rating → INSERT mới
            rating = Rating.builder()
                    .user(user)
                    .story(story)
                    .score(request.getScore())
                    .build();
            ratingRepository.save(rating);
            log.info("User '{}' rated story '{}': {} stars", username, story.getTitle(), request.getScore());
        } else {
            // Đã UPDATE thành công → load lại
            rating = ratingRepository.findByUserIdAndStoryId(user.getId(), story.getId())
                    .orElseThrow();
            log.info("User '{}' updated rating for '{}': {} stars", username, story.getTitle(), request.getScore());
        }
 
        // Tính lại thống kê sau khi rate
        RatingResponse.ScoreDistribution distribution = buildDistribution(story.getId());
        Double avg = ratingRepository.findAvgScoreByStoryId(story.getId());
        long total = ratingRepository.countByStoryId(story.getId());
 
        return RatingResponse.builder()
                .ratingId(rating.getId())
                .myScore(rating.getScore())
                .isNew(isNew)
                .storyId(story.getId())
                .avgScore(roundAvg(avg))
                .totalRatings(total)
                .distribution(distribution)
                .build();
    }
 
    // ===== GET /api/ratings/{storyId} =====
    // Lấy thống kê rating của 1 truyện
    // Nếu user đã đăng nhập → trả về myScore, chưa đăng nhập → myScore = null
    @Transactional(readOnly = true)
    public StoryRatingResponse getStoryRating(Long storyId, String username) {
        Story story = getStory(storyId);
 
        Double avg = ratingRepository.findAvgScoreByStoryId(storyId);
        long total = ratingRepository.countByStoryId(storyId);
        RatingResponse.ScoreDistribution distribution = buildDistribution(storyId);
 
        // Tìm điểm của user hiện tại (nếu có)
        Integer myScore = null;
        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                myScore = ratingRepository
                        .findByUserIdAndStoryId(user.get().getId(), storyId)
                        .map(Rating::getScore)
                        .orElse(null);
            }
        }
 
        return StoryRatingResponse.builder()
                .storyId(story.getId())
                .storyTitle(story.getTitle())
                .avgScore(roundAvg(avg))
                .totalRatings(total)
                .distribution(distribution)
                .myScore(myScore)
                .build();
    }
 
    // ===== Helpers =====
 
    // Đếm số rating theo từng mức 1-5 sao
    private RatingResponse.ScoreDistribution buildDistribution(Long storyId) {
        return RatingResponse.ScoreDistribution.builder()
                .oneStar(ratingRepository.countByStoryIdAndScore(storyId, 1))
                .twoStar(ratingRepository.countByStoryIdAndScore(storyId, 2))
                .threeStar(ratingRepository.countByStoryIdAndScore(storyId, 3))
                .fourStar(ratingRepository.countByStoryIdAndScore(storyId, 4))
                .fiveStar(ratingRepository.countByStoryIdAndScore(storyId, 5))
                .build();
    }
 
    // Làm tròn điểm trung bình 1 chữ số thập phân (vd: 4.3)
    private Double roundAvg(Double avg) {
        if (avg == null) return 0.0;
        return BigDecimal.valueOf(avg)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
 
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Không tìm thấy user", HttpStatus.NOT_FOUND));
    }
 
    private Story getStory(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy truyện với id: " + storyId, HttpStatus.NOT_FOUND));
    }
}
