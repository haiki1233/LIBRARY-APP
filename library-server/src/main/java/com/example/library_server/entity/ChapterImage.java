package com.example.library_server.entity;

import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "chapter_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterImage {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;
 
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
 
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
