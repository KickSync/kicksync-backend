package be.kicksync_backend.feature.product.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.product.repository.DropEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DropEventService {
    private final DropEventRepository dropEventRepository;

    public void findById(Long eventId) {
        dropEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.DROP_EVENT_NOT_FOUND));
    }
}