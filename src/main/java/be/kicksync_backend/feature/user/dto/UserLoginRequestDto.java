package be.kicksync_backend.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {
    @NotBlank(message = "아이디는 필수 사항입니다.")
    private String username;

    @NotBlank(message = "패스워드는 필수 사항입니다.")
    private String password;
}