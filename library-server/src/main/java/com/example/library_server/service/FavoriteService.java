package com.example.library_server.service;

import com.example.library_server.dto.response.FavoriteResponse;
import com.example.library_server.dto.response.FavoriteStatusResponse;
import com.example.library_server.dto.response.GenreResponse;
import com.example.library_server.dto.response.PageResponse;
import com.example.library_server.entity.Favorite;
import com.example.library_server.entity.Story;
import com.example.library_server.entity.User;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.FavoriteRepository;
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
public class FavoriteService {
 
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
 
    // ===== POST /api/favorites/{storyId} =====
    // Thêm truyện vào danh sách yêu thích
    @Transactional
    public FavoriteStatusResponse addFavorite(String username, Long storyId) {
        User user = getUser(username);
        Story story = getStory(storyId);
 
        // Kiểm tra đã favorite chưa - nếu rồi thì báo lỗi
        if (favoriteRepository.existsByUserIdAndStoryId(user.getId(), storyId)) {
            throw new AppException("Truyện này đã có trong danh sách yêu thích", HttpStatus.CONFLICT);
        }
 
        Favorite favorite = Favorite.builder()
                .user(user)
                .story(story)
                .build();
 
        favoriteRepository.save(favorite);
        long total = favoriteRepository.countByStoryId(storyId);
 
        log.info("User '{}' added story {} to favorites", username, storyId);
 
        return FavoriteStatusResponse.builder()
                .storyId(storyId)
                .isFavorited(true)
                .totalFavorites(total)
                .build();
    }
 
    // ===== DELETE /api/favorites/{storyId} =====
    // Bỏ truyện khỏi danh sách yêu thích
    @Transactional
    public FavoriteStatusResponse removeFavorite(String username, Long storyId) {
        User user = getUser(username);
 
        // Kiểm tra có trong favorite không
        if (!favoriteRepository.existsByUserIdAndStoryId(user.getId(), storyId)) {
            throw new AppException("Truyện này chưa có trong danh sách yêu thích", HttpStatus.NOT_FOUND);
        }
 
        favoriteRepository.deleteByUserIdAndStoryId(user.getId(), storyId);
        long total = favoriteRepository.countByStoryId(storyId);
 
        log.info("User '{}' removed story {} from favorites", username, storyId);
 
        return FavoriteStatusResponse.builder()
                .storyId(storyId)
                .isFavorited(false)
                .totalFavorites(total)
                .build();
    }
 
    // ===== GET /api/favorites =====
    // Lấy danh sách tất cả truyện yêu thích của user (có phân trang)
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getMyFavorites(String username, int page, int size) {
        User user = getUser(username);
 
        Page<Favorite> favorites = favoriteRepository.findByUserIdWithStory(
                user.getId(), PageRequest.of(page, size));
 
        return PageResponse.of(favorites.map(this::mapToResponse));
    }
 
    // ===== Mapper =====
    private FavoriteResponse mapToResponse(Favorite favorite) {
        Story story = favorite.getStory();
 
        List<GenreResponse> genres = story.getGenres().stream()
                .map(g -> GenreResponse.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .build())
                .toList();
 
        double avgRating = story.getRatings() == null || story.getRatings().isEmpty() ? 0.0
                : story.getRatings().stream().mapToInt(r -> r.getScore()).average().orElse(0.0);
 
        return FavoriteResponse.builder()
                .favoriteId(favorite.getId())
                .savedAt(favorite.getCreatedAt())
                .storyId(story.getId())
                .storyTitle(story.getTitle())
                .storyAuthor(story.getAuthor())
                .storyCoverImage(story.getCoverImage())
                .storyStatus(story.getStatus())
                .totalChapters(story.getChapters().size())
                .avgRating(avgRating)
                .genres(genres)
                .storyUpdatedAt(story.getUpdatedAt())
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