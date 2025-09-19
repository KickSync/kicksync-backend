package be.kicksync_backend.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileUpdateRequestDto {
    @NotBlank(message = "닉네임은 필수 사항입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 최소 2자 이상, 20자 이하로 작성해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 영문, 한글, 숫자로만 구성되어야 합니다.")
    private String nickname;
}
