package com.example.library_server.repository;

import com.example.library_server.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
 
    // Tìm rating của 1 user cho 1 truyện (upsert)
    Optional<Rating> findByUserIdAndStoryId(Long userId, Long storyId);
 
    // Kiểm tra user đã rating chưa
    boolean existsByUserIdAndStoryId(Long userId, Long storyId);
 
    // Đếm tổng số rating của truyện
    long countByStoryId(Long storyId);
 
    // Tính điểm trung bình
    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.story.id = :storyId")
    Double findAvgScoreByStoryId(@Param("storyId") Long storyId);
 
    // Đếm số rating theo từng mức điểm (dùng cho distribution)
    long countByStoryIdAndScore(Long storyId, Integer score);
 
    // Update score nếu đã rating rồi (upsert pattern)
    @Modifying
    @Query("UPDATE Rating r SET r.score = :score WHERE r.user.id = :userId AND r.story.id = :storyId")
    int updateScore(
            @Param("userId") Long userId,
            @Param("storyId") Long storyId,
            @Param("score") Integer score);
}