package be.kicksync_backend.feature.notification.controller;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.notification.dto.DropResultDto;
import be.kicksync_backend.feature.notification.service.NotificationService;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Validated
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @MessageMapping("/test-broadcast/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public void testBroadcast(@DestinationVariable Long eventId, @Valid DropResultDto message, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        notificationService.sendDropResult(eventId, user.getId(), message);
    }
}