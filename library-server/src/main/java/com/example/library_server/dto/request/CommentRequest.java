package com.example.library_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "storyId không được để trống")
    private Long storyId;

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(min = 1, max = 1000, message = "Bình luận tối đa 1000 ký tự")
    private String content;

    // Optional - nếu có thì là reply, không có thì là comment gốc
    private Long parentId;
}