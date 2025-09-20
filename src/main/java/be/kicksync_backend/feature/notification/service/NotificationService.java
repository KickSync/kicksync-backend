package be.kicksync_backend.feature.notification.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.notification.dto.DropResultDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserRepository userRepository;

    private static final String DEST_PREFIX = "/queue/drop/";


    @Transactional(readOnly = true)
    public void sendDropResult(Long eventId, Long userId, DropResultDto result) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        simpMessagingTemplate.convertAndSendToUser(user.getUsername(), DEST_PREFIX + eventId, result);
    }
}