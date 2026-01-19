package be.kicksync_backend.feature.partner.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.partner.dto.PartnerSignupRequestDto;
import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.partner.repository.PartnerRepository;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.entity.Role;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerAuthService {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto signup(PartnerSignupRequestDto requestDto) {
        if (!requestDto.getUsername().startsWith("pt_")) {
            throw new CustomException(ErrorCode.INVALID_USERNAME_PATTERN);
        }

        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        User user = new User(requestDto.getUsername(), encodedPassword, Role.PARTNER);
        User savedUser = userRepository.save(user);

        Partner partner = Partner.builder()
                .name(requestDto.getPartnerName())
                .businessNumber(requestDto.getBusinessNumber())
                .commissionRate(BigDecimal.valueOf(0.05))
                .user(savedUser)
                .build();
        
        partnerRepository.save(partner);

        return new UserResponseDto(savedUser);
    }
}
