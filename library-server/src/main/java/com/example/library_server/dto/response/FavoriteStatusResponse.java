package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteStatusResponse {
 
    private Long storyId;
    private boolean isFavorited;        // true = đã lưu, false = đã bỏ
    private long totalFavorites;        // Tổng số người đã yêu thích truyện này
}
