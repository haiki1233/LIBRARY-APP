package com.example.library_server.service;

import com.example.library_server.dto.request.CommentRequest;
import com.example.library_server.dto.response.CommentResponse;
import com.example.library_server.dto.response.PageResponse;
import com.example.library_server.entity.Comment;
import com.example.library_server.entity.Story;
import com.example.library_server.entity.User;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.CommentRepository;
import com.example.library_server.repository.StoryRepository;
import com.example.library_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    // ===== GET /api/comments/{storyId} =====
    // Lấy danh sách comment gốc có phân trang
    // Mỗi comment gốc kèm theo tối đa 3 reply đầu tiên
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByStory(Long storyId, int page, int size) {
        // Kiểm tra truyện có tồn tại không
        if (!storyRepository.existsById(storyId)) {
            throw new AppException("Không tìm thấy truyện với id: " + storyId, HttpStatus.NOT_FOUND);
        }

        Page<Comment> comments = commentRepository.findRootCommentsByStoryId(
                storyId, PageRequest.of(page, size));

        return PageResponse.of(comments.map(this::mapToResponse));
    }

    // ===== POST /api/comments =====
    // Đăng comment mới hoặc reply
    @Transactional
    public CommentResponse postComment(String username, CommentRequest request) {
        User user = getUser(username);
        Story story = getStory(request.getStoryId());

        Comment.CommentBuilder builder = Comment.builder()
                .user(user)
                .story(story)
                .content(request.getContent().trim());

        // Nếu có parentId → là reply
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(
                            "Không tìm thấy comment cha", HttpStatus.NOT_FOUND));

            // Chỉ cho reply 1 cấp (reply của reply vẫn gắn vào comment gốc)
            // Tránh thread quá sâu, khó quản lý
            Comment rootParent = (parent.getParent() != null) ? parent.getParent() : parent;
            builder.parent(rootParent);

            log.info("User '{}' replied to comment {} on story '{}'",
                    username, rootParent.getId(), story.getTitle());
        } else {
            log.info("User '{}' commented on story '{}'", username, story.getTitle());
        }

        Comment saved = commentRepository.save(builder.build());
        return mapToResponse(saved);
    }

    // ===== Mapper =====
    private CommentResponse mapToResponse(Comment comment) {
        // Nếu đã xóa → ẩn nội dung nhưng vẫn giữ node (để reply bên dưới không bị mất)
        String content = Boolean.TRUE.equals(comment.getIsDeleted())
                ? "[Bình luận đã bị xóa]"
                : comment.getContent();

        boolean isEdited = comment.getUpdatedAt() != null
                && comment.getUpdatedAt().isAfter(comment.getCreatedAt());

        // Load replies (chỉ 1 cấp, sắp xếp cũ nhất lên trước)
        List<Comment> replyEntities = commentRepository.findRepliesByParentId(comment.getId());

        List<CommentResponse> replies = replyEntities.stream()
                .map(reply -> {
                    String replyContent = Boolean.TRUE.equals(reply.getIsDeleted())
                            ? "[Bình luận đã bị xóa]"
                            : reply.getContent();

                    return CommentResponse.builder()
                            .id(reply.getId())
                            .content(replyContent)
                            .isDeleted(Boolean.TRUE.equals(reply.getIsDeleted()))
                            .isEdited(reply.getUpdatedAt().isAfter(reply.getCreatedAt()))
                            .createdAt(reply.getCreatedAt())
                            .updatedAt(reply.getUpdatedAt())
                            .author(buildAuthor(reply.getUser()))
                            .replies(List.of())   // Không đệ quy thêm
                            .replyCount(0)
                            .build();
                })
                .toList();

        return CommentResponse.builder()
                .id(comment.getId())
                .content(content)
                .isDeleted(Boolean.TRUE.equals(comment.getIsDeleted()))
                .isEdited(isEdited)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .author(buildAuthor(comment.getUser()))
                .replies(replies)
                .replyCount(replies.size())
                .build();
    }

    private CommentResponse.AuthorInfo buildAuthor(User user) {
        return CommentResponse.AuthorInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .build();
    }

    // ===== Helpers =====
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