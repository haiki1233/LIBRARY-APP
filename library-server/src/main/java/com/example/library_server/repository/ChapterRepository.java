package com.example.library_server.repository;

import com.example.library_server.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
 
    // Lấy tất cả chapter của 1 truyện, sắp xếp theo số chapter tăng dần
    List<Chapter> findByStoryIdOrderByChapterNumberAsc(Long storyId);
 
    // Lấy chapter kèm theo ảnh (tránh N+1 query)
    @Query("""
        SELECT c FROM Chapter c
        LEFT JOIN FETCH c.images
        WHERE c.id = :id
        ORDER BY c.id
        """)
    Optional<Chapter> findByIdWithImages(@Param("id") Long id);
 
    // Lấy chapter trước (để điều hướng prev/next)
    @Query("""
        SELECT c FROM Chapter c
        WHERE c.story.id = :storyId
          AND c.chapterNumber < :currentNumber
        ORDER BY c.chapterNumber DESC
        LIMIT 1
        """)
    Optional<Chapter> findPrevChapter(
            @Param("storyId") Long storyId,
            @Param("currentNumber") Integer currentNumber);
 
    // Lấy chapter sau (để điều hướng prev/next)
    @Query("""
        SELECT c FROM Chapter c
        WHERE c.story.id = :storyId
          AND c.chapterNumber > :currentNumber
        ORDER BY c.chapterNumber ASC
        LIMIT 1
        """)
    Optional<Chapter> findNextChapter(
            @Param("storyId") Long storyId,
            @Param("currentNumber") Integer currentNumber);
 
    // Đếm tổng chapter của 1 truyện
    long countByStoryId(Long storyId);
 
    // Kiểm tra story có tồn tại không (tránh query thêm StoryRepository)
    boolean existsByStoryId(Long storyId);
}
