package be.kicksync_backend.feature.notification.controller;

import be.kicksync_backend.feature.notification.dto.DropResultDto;
import be.kicksync_backend.feature.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final NotificationService notificationService;

    @MessageMapping("/test-broadcast/{eventId}")
    public void testBroadcast(@DestinationVariable Long eventId, DropResultDto message) {
        notificationService.sendDropResult(eventId, message);
    }
} 