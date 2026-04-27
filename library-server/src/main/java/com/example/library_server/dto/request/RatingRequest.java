package com.example.library_server.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
@Data
public class RatingRequest {
 
    @NotNull(message = "storyId không được để trống")
    private Long storyId;
 
    @NotNull(message = "score không được để trống")
    @Min(value = 1, message = "Điểm đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá tối đa là 5 sao")
    private Integer score;
}