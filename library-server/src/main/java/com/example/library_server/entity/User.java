package com.example.library_server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
 
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
 
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true, length = 50)
    private String username;
 
    @Column(nullable = false, unique = true, length = 100)
    private String email;
 
    @Column(nullable = false)
    private String password;
 
    @Column(length = 255)
    private String avatar;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;
 
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
 
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
 
    // ===== UserDetails implementation =====
 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
 
    @Override
    public boolean isAccountNonExpired() { return true; }
 
    @Override
    public boolean isAccountNonLocked() { return true; }
 
    @Override
    public boolean isCredentialsNonExpired() { return true; }
 
    @Override
    public boolean isEnabled() { return isActive; }
 
    public enum Role {
        USER, ADMIN
    }
}
