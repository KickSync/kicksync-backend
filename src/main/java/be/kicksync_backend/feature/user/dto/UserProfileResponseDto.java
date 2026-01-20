package be.kicksync_backend.feature.user.dto;

import be.kicksync_backend.feature.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserProfileResponseDto {
    @Schema(description = "사용자 아이디", example = "user123")
    private final String username;
    
    @Schema(description = "닉네임", example = "nickname123")
    private final String nickname;

    public UserProfileResponseDto(User user) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
    }
}
