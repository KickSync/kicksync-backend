package be.kicksync_backend.feature.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequestDto {
    @Schema(description = "사용자 아이디 (소문자 + 숫자, 6~20자)", example = "user123")
    @NotBlank(message = "아이디는 필수 사항입니다.")
    @Size(min = 6, max = 20, message = "아이디는 최소 6자 이상, 20자 이하로 작성해주세요.")
    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 소문자 + 숫자로 구성되어야 합니다.")
    private String username;

    @Schema(description = "사용자 비밀번호 (알파벳 대소문자 + 숫자 + 특수문자, 6자 이상)", example = "Password123!")
    @NotBlank(message = "패스워드는 필수 사항입니다.")
    @Size(min = 6, max = 255, message = "비밀번호는 최소 6글자 이상, 255글자 이하이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])\\S{6,}$", message = "비밀번호는 알파벳 대소문자 + 숫자 + 특수문자로만 구성되어야 합니다.")
    private String password;
}