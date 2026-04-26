package com.example.library_server.repository;

import com.example.library_server.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
 
    // Tìm favorite theo userId + storyId
    Optional<Favorite> findByUserIdAndStoryId(Long userId, Long storyId);
 
    // Kiểm tra user đã favorite truyện chưa
    boolean existsByUserIdAndStoryId(Long userId, Long storyId);
 
    // Lấy danh sách favorite của user, kèm story + genres (tránh N+1)
    @Query("""
        SELECT f FROM Favorite f
        JOIN FETCH f.story s
        LEFT JOIN FETCH s.genres
        WHERE f.user.id = :userId
        ORDER BY f.createdAt DESC
        """)
    Page<Favorite> findByUserIdWithStory(@Param("userId") Long userId, Pageable pageable);
 
    // Đếm tổng favorite của 1 truyện (hiển thị số lượt yêu thích)
    long countByStoryId(Long storyId);
 
    // Xóa theo userId + storyId
    void deleteByUserIdAndStoryId(Long userId, Long storyId);
}
