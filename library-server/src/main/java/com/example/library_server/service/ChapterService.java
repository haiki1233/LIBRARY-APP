package com.example.library_server.service;

import com.example.library_server.dto.response.ChapterDetailResponse;
import com.example.library_server.dto.response.ChapterListResponse;
import com.example.library_server.entity.Chapter;
import com.example.library_server.entity.Story;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.ChapterRepository;
import com.example.library_server.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.Optional;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class ChapterService {
 
    private final ChapterRepository chapterRepository;
    private final StoryRepository storyRepository;
 
    // ===== GET /api/chapters/{storyId} =====
    // Lấy danh sách tất cả chapter của 1 truyện
    @Transactional(readOnly = true)
    public ChapterListResponse getChaptersByStory(Long storyId) {
        // Kiểm tra truyện có tồn tại không
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy truyện với id: " + storyId, HttpStatus.NOT_FOUND));
 
        List<Chapter> chapters = chapterRepository.findByStoryIdOrderByChapterNumberAsc(storyId);
        long total = chapterRepository.countByStoryId(storyId);
 
        // Map từng chapter sang ChapterItemResponse
        List<ChapterListResponse.ChapterItemResponse> items = chapters.stream()
                .map(c -> ChapterListResponse.ChapterItemResponse.builder()
                        .id(c.getId())
                        .chapterNumber(c.getChapterNumber())
                        .title(c.getTitle())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
 
        log.info("Story {} has {} chapters", storyId, total);
 
        return ChapterListResponse.builder()
                .storyId(story.getId())
                .storyTitle(story.getTitle())
                .storyCoverImage(story.getCoverImage())
                .totalChapters(total)
                .chapters(items)
                .build();
    }
 
    // ===== GET /api/chapter/{id} =====
    // Lấy chi tiết 1 chapter để đọc (kèm ảnh + điều hướng)
    @Transactional(readOnly = true)
    public ChapterDetailResponse getChapterDetail(Long chapterId) {
        // Lấy chapter kèm ảnh (1 query duy nhất - tránh N+1)
        Chapter chapter = chapterRepository.findByIdWithImages(chapterId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chapter với id: " + chapterId, HttpStatus.NOT_FOUND));
 
        Story story = chapter.getStory();
 
        // Map danh sách ảnh, sắp xếp theo order_index
        List<ChapterDetailResponse.ChapterImageResponse> images = chapter.getImages()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(img -> ChapterDetailResponse.ChapterImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .orderIndex(img.getOrderIndex())
                        .build())
                .toList();
 
        // Tìm chapter trước và sau để điều hướng
        Optional<Chapter> prevChapter = chapterRepository.findPrevChapter(
                story.getId(), chapter.getChapterNumber());
        Optional<Chapter> nextChapter = chapterRepository.findNextChapter(
                story.getId(), chapter.getChapterNumber());
 
        ChapterDetailResponse.Navigation navigation = ChapterDetailResponse.Navigation.builder()
                .prevChapterId(prevChapter.map(Chapter::getId).orElse(null))
                .prevChapterNumber(prevChapter.map(Chapter::getChapterNumber).orElse(null))
                .nextChapterId(nextChapter.map(Chapter::getId).orElse(null))
                .nextChapterNumber(nextChapter.map(Chapter::getChapterNumber).orElse(null))
                .build();
 
        log.info("Chapter {} of story '{}' loaded ({} images)",
                chapter.getChapterNumber(), story.getTitle(), images.size());
 
        return ChapterDetailResponse.builder()
                .id(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .createdAt(chapter.getCreatedAt())
                .storyId(story.getId())
                .storyTitle(story.getTitle())
                .images(images)
                .navigation(navigation)
                .build();
    }
}
