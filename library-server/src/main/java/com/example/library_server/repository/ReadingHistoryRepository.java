package com.example.library_server.repository;

import com.example.library_server.entity.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.time.LocalDateTime;
import java.util.Optional;
 
@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
 
    // Tìm history theo userId + chapterId (để upsert)
    Optional<ReadingHistory> findByUserIdAndChapterId(Long userId, Long chapterId);
 
    // Lấy lịch sử đọc của user, kèm chapter + story (tránh N+1)
    // Group theo story - lấy chapter mới nhất mỗi truyện
    @Query("""
        SELECT h FROM ReadingHistory h
        JOIN FETCH h.chapter c
        JOIN FETCH c.story s
        WHERE h.user.id = :userId
        ORDER BY h.lastReadAt DESC
        """)
    Page<ReadingHistory> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);
 
    // Kiểm tra history có thuộc về user không (dùng trước khi xóa)
    boolean existsByIdAndUserId(Long id, Long userId);
 
    // Xóa toàn bộ lịch sử của user (tùy chọn)
    @Modifying
    @Query("DELETE FROM ReadingHistory h WHERE h.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
 
    // Update lastReadAt + scrollPosition thay vì insert mới (upsert pattern)
    @Modifying
    @Query("""
        UPDATE ReadingHistory h
        SET h.lastReadAt = :lastReadAt,
            h.scrollPosition = :scrollPosition
        WHERE h.user.id = :userId
          AND h.chapter.id = :chapterId
        """)
    int updateHistory(
            @Param("userId") Long userId,
            @Param("chapterId") Long chapterId,
            @Param("scrollPosition") Integer scrollPosition,
            @Param("lastReadAt") LocalDateTime lastReadAt);
}