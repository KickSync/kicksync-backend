package be.kicksync_backend.feature.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginRequestDto {
    @Schema(description = "관리자 아이디", example = "admin123")
    @NotBlank(message = "아이디는 필수 사항입니다.")
    private String username;

    @Schema(description = "관리자 비밀번호", example = "Password123!")
    @NotBlank(message = "패스워드는 필수 사항입니다.")
    private String password;
}
