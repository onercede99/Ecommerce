package com.codegym.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data // <-- Annotation này rất quan trọng, nó tự tạo getter và setter
public class PasswordChangeDto {

    @NotEmpty(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword; // <-- Tên trường phải là 'currentPassword'

    @NotEmpty(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    private String newPassword; // <-- Tên trường phải là 'newPassword'

    @NotEmpty(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword; // <-- Tên trường phải là 'confirmPassword'
}