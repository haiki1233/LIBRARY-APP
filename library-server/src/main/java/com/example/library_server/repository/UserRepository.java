package com.example.library_server.repository;

import com.example.library_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
 
    Optional<User> findByUsername(String username);
 
    Optional<User> findByEmail(String email);
 
    // Tìm theo username hoặc email (dùng cho login linh hoạt)
    Optional<User> findByUsernameOrEmail(String username, String email);
 
    boolean existsByUsername(String username);
 
    boolean existsByEmail(String email);
}