package com.example.library_server.repository;

import com.example.library_server.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Lấy comment gốc (parent = null) của truyện, kèm user (tránh N+1)
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.user
        WHERE c.story.id = :storyId
          AND c.parent IS NULL
        ORDER BY c.createdAt DESC
        """)
    Page<Comment> findRootCommentsByStoryId(
            @Param("storyId") Long storyId, Pageable pageable);

    // Lấy tất cả reply của 1 comment cha, kèm user
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.user
        WHERE c.parent.id = :parentId
        ORDER BY c.createdAt ASC
        """)
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    // Đếm tổng comment gốc của truyện (không tính reply)
    long countByStoryIdAndParentIsNull(Long storyId);

    // Đếm reply của 1 comment
    long countByParentId(Long parentId);

    // Kiểm tra comment thuộc về user (trước khi xóa/sửa)
    boolean existsByIdAndUserId(Long id, Long userId);
}