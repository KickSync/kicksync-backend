package be.kicksync_backend.feature.user.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.user.dto.ProfileUpdateRequestDto;
import be.kicksync_backend.feature.user.dto.UserProfileResponseDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new UserProfileResponseDto(user);
    }

    public void updateProfile(String username, ProfileUpdateRequestDto requestDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (Objects.equals(user.getNickname(), requestDto.getNickname())) {
            return;
        }

        userRepository.findByNickname(requestDto.getNickname()).ifPresent(u -> {
            if (!u.getId().equals(user.getId())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
        });

        user.updateNickname(requestDto.getNickname());
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrderHistory(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return orderRepository.findByUser(user, pageable)
                .map(OrderResponseDto::new);
    }
}
