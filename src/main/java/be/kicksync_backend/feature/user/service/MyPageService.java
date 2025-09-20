package be.kicksync_backend.feature.user.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.user.dto.ProfileUpdateRequestDto;
import be.kicksync_backend.feature.user.dto.UserProfileResponseDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return new UserProfileResponseDto(user);
    }

    public void updateProfile(String username, ProfileUpdateRequestDto requestDto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userRepository.findByNickname(requestDto.getNickname()).ifPresent(u -> {
            if (!u.getId().equals(user.getId())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
        });

        user.updateNickname(requestDto.getNickname());
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrderHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return user.getOrders().stream()
                .map(OrderResponseDto::new)
                .collect(Collectors.toList());
    }
}
