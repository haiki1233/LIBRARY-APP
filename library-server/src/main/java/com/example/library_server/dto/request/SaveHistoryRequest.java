package com.example.library_server.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
@Data
public class SaveHistoryRequest {
 
    @NotNull(message = "chapterId không được để trống")
    private Long chapterId;
 
    // Vị trí scroll (0-100%), mặc định 0 nếu không truyền
    @Min(value = 0, message = "scrollPosition phải >= 0")
    @Max(value = 100, message = "scrollPosition phải <= 100")
    private Integer scrollPosition = 0;
}
