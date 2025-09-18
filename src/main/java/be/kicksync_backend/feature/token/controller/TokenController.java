package be.kicksync_backend.feature.token.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.token.RefreshTokenService;
import be.kicksync_backend.feature.token.dto.TokenRefreshRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponseDto>> refreshToken(@RequestBody TokenRefreshRequestDto requestDto) {
        String requestRefreshToken = requestDto.getRefreshToken();

        JwtResponseDto newTokens = refreshTokenService.refreshTokens(requestRefreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        ApiResponse<JwtResponseDto> response = ApiResponse.<JwtResponseDto>builder()
                .msg(ResponseText.TOKEN_REFRESH_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(newTokens)
                .build();
        return ResponseEntity.ok(response);
    }

} 