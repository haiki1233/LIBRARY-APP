package com.example.library_server.service;

import com.example.library_server.dto.request.SaveHistoryRequest;
import com.example.library_server.dto.response.HistoryResponse;
import com.example.library_server.dto.response.PageResponse;
import com.example.library_server.entity.Chapter;
import com.example.library_server.entity.ReadingHistory;
import com.example.library_server.entity.Story;
import com.example.library_server.entity.User;
import com.example.library_server.exception.AppException;
import com.example.library_server.repository.ChapterRepository;
import com.example.library_server.repository.ReadingHistoryRepository;
import com.example.library_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.LocalDateTime;
import java.util.Optional;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {
 
    private final ReadingHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
 
    // ===== POST /api/history =====
    // Lưu / cập nhật lịch sử đọc (UPSERT)
    // - Nếu chưa đọc chapter này → INSERT
    // - Nếu đã đọc rồi → UPDATE lastReadAt + scrollPosition
    @Transactional
    public HistoryResponse saveHistory(String username, SaveHistoryRequest request) {
        User user = getUser(username);
        Chapter chapter = getChapter(request.getChapterId());
 
        LocalDateTime now = LocalDateTime.now();
        int scrollPosition = request.getScrollPosition() != null
                ? request.getScrollPosition() : 0;
 
        // Thử UPDATE trước
        int updated = historyRepository.updateHistory(
                user.getId(), chapter.getId(), scrollPosition, now);
 
        ReadingHistory history;
 
        if (updated == 0) {
            // Chưa có record → INSERT mới
            history = ReadingHistory.builder()
                    .user(user)
                    .chapter(chapter)
                    .scrollPosition(scrollPosition)
                    .lastReadAt(now)
                    .build();
            history = historyRepository.save(history);
            log.info("User '{}' started reading chapter {} of story '{}'",
                    username, chapter.getChapterNumber(), chapter.getStory().getTitle());
        } else {
            // Đã UPDATE → load lại để trả về response
            history = historyRepository.findByUserIdAndChapterId(user.getId(), chapter.getId())
                    .orElseThrow();
            log.info("User '{}' updated reading position: chapter {}, scroll {}%",
                    username, chapter.getChapterNumber(), scrollPosition);
        }
 
        return mapToResponse(history);
    }
 
    // ===== GET /api/history =====
    // Lấy lịch sử đọc của user, sắp xếp mới nhất lên đầu
    @Transactional(readOnly = true)
    public PageResponse<HistoryResponse> getMyHistory(String username, int page, int size) {
        User user = getUser(username);
 
        Page<ReadingHistory> histories = historyRepository.findByUserIdWithDetails(
                user.getId(), PageRequest.of(page, size));
 
        return PageResponse.of(histories.map(this::mapToResponse));
    }
 
    // ===== DELETE /api/history/{id} =====
    // Xóa 1 mục lịch sử (chỉ xóa được của chính mình)
    @Transactional
    public void deleteHistory(String username, Long historyId) {
        User user = getUser(username);
 
        // Kiểm tra record có tồn tại và thuộc về user không
        if (!historyRepository.existsByIdAndUserId(historyId, user.getId())) {
            throw new AppException(
                    "Không tìm thấy lịch sử hoặc bạn không có quyền xóa", HttpStatus.NOT_FOUND);
        }
 
        historyRepository.deleteById(historyId);
        log.info("User '{}' deleted history record {}", username, historyId);
    }
 
    // ===== Mapper =====
    private HistoryResponse mapToResponse(ReadingHistory h) {
        Chapter chapter = h.getChapter();
        Story story = chapter.getStory();
 
        return HistoryResponse.builder()
                .historyId(h.getId())
                .lastReadAt(h.getLastReadAt())
                .scrollPosition(h.getScrollPosition())
                // Chapter info
                .chapterId(chapter.getId())
                .chapterNumber(chapter.getChapterNumber())
                .chapterTitle(chapter.getTitle())
                // Story info
                .storyId(story.getId())
                .storyTitle(story.getTitle())
                .storyAuthor(story.getAuthor())
                .storyCoverImage(story.getCoverImage())
                .storyStatus(story.getStatus())
                .totalChapters(story.getChapters().size())
                .build();
    }
 
    // ===== Helpers =====
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Không tìm thấy user", HttpStatus.NOT_FOUND));
    }
 
    private Chapter getChapter(Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new AppException(
                        "Không tìm thấy chapter với id: " + chapterId, HttpStatus.NOT_FOUND));
    }
}