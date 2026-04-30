package com.example.library_server.repository;

import com.example.library_server.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
 
    // Tìm kiếm theo title hoặc author (không phân biệt hoa thường)
    @Query("""
        SELECT s FROM Story s
        WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Story> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
 
    // Lọc theo genre
    @Query("""
        SELECT s FROM Story s
        JOIN s.genres g
        WHERE g.id = :genreId
        """)
    Page<Story> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);
 
    // Lọc theo status
    Page<Story> findByStatus(Story.StoryStatus status, Pageable pageable);
 
    // Tăng view count
    @Modifying
    @Query("UPDATE Story s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
