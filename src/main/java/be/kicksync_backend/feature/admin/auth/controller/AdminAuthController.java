package be.kicksync_backend.feature.admin.auth.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.feature.admin.auth.dto.AdminLoginRequestDto;
import be.kicksync_backend.feature.admin.auth.dto.AdminSignupRequestDto;
import be.kicksync_backend.feature.admin.auth.service.AdminAuthService;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
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

@Tag(name = "Admin Auth", description = "관리자 인증 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    @Operation(summary = "관리자 회원가입", description = "관리자 계정을 생성합니다. 관리자 키가 필요합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 키 불일치")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(@Valid @RequestBody AdminSignupRequestDto requestDto) {
        UserResponseDto userResponseDto = adminAuthService.signup(requestDto);
        ApiResponse<UserResponseDto> response = ApiResponse.<UserResponseDto>builder()
                .msg("관리자 회원가입 성공")
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(userResponseDto)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "관리자 로그인", description = "관리자 로그인을 수행합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDto>> login(@Valid @RequestBody AdminLoginRequestDto requestDto) {
        JwtResponseDto jwtResponseDto = adminAuthService.login(requestDto);
        ApiResponse<JwtResponseDto> response = ApiResponse.<JwtResponseDto>builder()
                .msg("관리자 로그인 성공")
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(jwtResponseDto)
                .build();
        return ResponseEntity.ok(response);
    }
}
