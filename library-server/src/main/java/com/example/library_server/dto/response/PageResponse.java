package com.example.library_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
 
import java.util.List;
 
// Wrapper chung cho tất cả response có phân trang
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
 
    private List<T> content;
    private int pageNumber;    // Trang hiện tại (bắt đầu từ 0)
    private int pageSize;      // Số item mỗi trang
    private long totalElements; // Tổng số item
    private int totalPages;    // Tổng số trang
    private boolean isFirst;
    private boolean isLast;
 
    // Factory method từ Spring Page
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
