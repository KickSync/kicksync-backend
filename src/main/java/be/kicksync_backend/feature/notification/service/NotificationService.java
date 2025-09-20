package be.kicksync_backend.feature.notification.service;

import be.kicksync_backend.feature.notification.dto.DropResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void sendDropResult(Long eventId, DropResultDto result) {
        simpMessagingTemplate.convertAndSend("/topic/drop/" + eventId, result);
    }
} 