package com.example.library_server.entity;

import jakarta.persistence.*;
import lombok.*;
 
import java.util.HashSet;
import java.util.Set;
 
@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Genre {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true, length = 50)
    private String name;
 
    @ManyToMany(mappedBy = "genres")
    @Builder.Default
    private Set<Story> stories = new HashSet<>();
}
