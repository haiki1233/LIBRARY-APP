package com.example.library_server.entity;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
 
@Entity
@Table(name = "reading_history",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "chapter_id"},
        name = "uk_history_user_chapter"  // Mỗi user chỉ có 1 record per chapter
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingHistory {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;
 
    // Thời điểm đọc gần nhất (UPDATE mỗi lần đọc lại)
    @Column(name = "last_read_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastReadAt = LocalDateTime.now();
 
    // Vị trí scroll cuối cùng (%) - để ghi nhớ đọc đến đâu
    @Column(name = "scroll_position")
    @Builder.Default
    private Integer scrollPosition = 0;
}