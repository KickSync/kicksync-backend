package be.kicksync_backend.feature.token.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.token.RefreshTokenService;
import be.kicksync_backend.feature.token.dto.TokenRefreshRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token", description = "토큰 관리 API")
@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용하여 새로운 Access Token을 발급받습니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponseDto>> refreshToken(@Valid @RequestBody TokenRefreshRequestDto requestDto) {
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