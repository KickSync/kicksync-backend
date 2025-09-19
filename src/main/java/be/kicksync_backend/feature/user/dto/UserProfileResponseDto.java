package be.kicksync_backend.feature.user.dto;

import be.kicksync_backend.feature.user.entity.User;
import lombok.Getter;

@Getter
public class UserProfileResponseDto {
    private final String username;
    private final String nickname;

    public UserProfileResponseDto(User user) {
        this.username = user.getUsername();
        this.nickname = user.getNickname();
    }
}
