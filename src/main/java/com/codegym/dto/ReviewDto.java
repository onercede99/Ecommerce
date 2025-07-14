package com.codegym.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class ReviewDto {
    @NotNull(message = "Vui lòng chọn số sao đánh giá.")
    @Min(value = 1, message = "Vui lòng chọn ít nhất 1 sao.")
    @Max(value = 5, message = "Điểm đánh giá không hợp lệ.")
    private Integer rating;

    @NotEmpty(message = "Nội dung đánh giá không được để trống.")
    @Size(max = 1000, message = "Nội dung không được vượt quá 1000 ký tự.")
    private String comment;
}