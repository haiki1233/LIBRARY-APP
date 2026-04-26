package com.example.library_server.entity;

import jakarta.persistence.*;
import lombok.*;
 
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
 
@Entity
@Table(name = "stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 255)
    private String title;
 
    @Column(length = 150)
    private String author;
 
    @Column(columnDefinition = "TEXT")
    private String description;
 
    @Column(name = "cover_image")
    private String coverImage;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StoryStatus status = StoryStatus.ONGOING;
 
    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;
 
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
 
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
 
    // Quan hệ Many-to-Many với Genre qua bảng story_genres
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "story_genres",
        joinColumns = @JoinColumn(name = "story_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();
 
    // Quan hệ One-to-Many với Chapter
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Chapter> chapters = new ArrayList<>();
 
    // Quan hệ One-to-Many với Rating
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();
 
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
 
    public enum StoryStatus {
        ONGOING,    // Đang ra
        COMPLETED   // Đã hoàn thành
    }
}
