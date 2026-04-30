package com.example.library_server.service;

import com.example.library_server.dto.response.*;
import com.example.library_server.entity.Genre;
import com.example.library_server.entity.Story;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.GenreRepository;
import com.example.library_server.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {
 
    private final StoryRepository storyRepository;
    private final GenreRepository genreRepository;
 
    // ===== GET /api/stories =====
    // Lấy danh sách tất cả truyện, có phân trang + sắp xếp
    @Transactional(readOnly = true)
    public PageResponse<StoryCardResponse> getAllStories(int page, int size, String sortBy) {
        Pageable pageable = buildPageable(page, size, sortBy);
        Page<Story> stories = storyRepository.findAll(pageable);
        return PageResponse.of(stories.map(this::mapToCard));
    }
 
    // ===== GET /api/stories/{id} =====
    // Lấy chi tiết 1 truyện + tăng view
    @Transactional
    public StoryDetailResponse getStoryById(Long id) {
        Story story = findStoryById(id);
 
        // Tăng view count mỗi lần xem chi tiết
        storyRepository.incrementViewCount(id);
 
        return mapToDetail(story);
    }
 
    // ===== GET /api/stories/search?q=... =====
    // Tìm kiếm theo title hoặc author
    @Transactional(readOnly = true)
    public PageResponse<StoryCardResponse> searchStories(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return getAllStories(page, size, "updatedAt");
        }
 
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Story> stories = storyRepository.searchByKeyword(keyword.trim(), pageable);
        log.info("Search '{}' → {} results", keyword, stories.getTotalElements());
 
        return PageResponse.of(stories.map(this::mapToCard));
    }
 
    // ===== GET /api/stories/genre/{id} =====
    // Lọc truyện theo thể loại
    @Transactional(readOnly = true)
    public PageResponse<StoryCardResponse> getStoriesByGenre(Long genreId, int page, int size) {
        // Kiểm tra genre có tồn tại không
        if (!genreRepository.existsById(genreId)) {
            throw new AppException("Thể loại không tồn tại", HttpStatus.NOT_FOUND);
        }
 
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Story> stories = storyRepository.findByGenreId(genreId, pageable);
 
        return PageResponse.of(stories.map(this::mapToCard));
    }
 
    // ===== Mapper: Story → StoryCardResponse =====
    private StoryCardResponse mapToCard(Story story) {
        return StoryCardResponse.builder()
                .id(story.getId())
                .title(story.getTitle())
                .author(story.getAuthor())
                .coverImage(story.getCoverImage())
                .status(story.getStatus())
                .viewCount(story.getViewCount())
                .avgRating(calcAvgRating(story))
                .totalChapters(story.getChapters().size())
                .genres(mapGenres(story))
                .updatedAt(story.getUpdatedAt())
                .build();
    }
 
    // ===== Mapper: Story → StoryDetailResponse =====
    private StoryDetailResponse mapToDetail(Story story) {
        List<StoryDetailResponse.ChapterSummaryResponse> chapterList = story.getChapters()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getChapterNumber(), b.getChapterNumber()))
                .map(c -> StoryDetailResponse.ChapterSummaryResponse.builder()
                        .id(c.getId())
                        .chapterNumber(c.getChapterNumber())
                        .title(c.getTitle())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
 
        return StoryDetailResponse.builder()
                .id(story.getId())
                .title(story.getTitle())
                .author(story.getAuthor())
                .description(story.getDescription())
                .coverImage(story.getCoverImage())
                .status(story.getStatus())
                .viewCount(story.getViewCount())
                .avgRating(calcAvgRating(story))
                .totalRatings(story.getRatings().size())
                .totalChapters(story.getChapters().size())
                .genres(mapGenres(story))
                .chapters(chapterList)
                .createdAt(story.getCreatedAt())
                .updatedAt(story.getUpdatedAt())
                .build();
    }
 
    // ===== Helpers =====
 
    private Story findStoryById(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy truyện với id: " + id, HttpStatus.NOT_FOUND));
    }
 
    private List<GenreResponse> mapGenres(Story story) {
        return story.getGenres().stream()
                .map(g -> GenreResponse.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .build())
                .toList();
    }
 
    private Double calcAvgRating(Story story) {
        if (story.getRatings() == null || story.getRatings().isEmpty()) return 0.0;
        return story.getRatings().stream()
                .mapToInt(r -> r.getScore())
                .average()
                .orElse(0.0);
    }
 
    private Pageable buildPageable(int page, int size, String sortBy) {
        Sort sort = switch (sortBy) {
            case "viewCount"  -> Sort.by("viewCount").descending();
            case "title"      -> Sort.by("title").ascending();
            case "createdAt"  -> Sort.by("createdAt").descending();
            default           -> Sort.by("updatedAt").descending(); // Mới cập nhật nhất
        };
        return PageRequest.of(page, size, sort);
    }
}
