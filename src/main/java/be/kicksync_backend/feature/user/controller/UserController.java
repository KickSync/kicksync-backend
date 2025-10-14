package be.kicksync_backend.feature.user.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.feature.user.dto.UserLoginRequestDto;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.dto.UserSignupRequestDto;
import be.kicksync_backend.feature.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import be.kicksync_backend.common.security.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 API
     *
     * @param requestDto 사용자 회원가입 요청 데이터
     * @return 생성된 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(@Valid @RequestBody UserSignupRequestDto requestDto) {
        UserResponseDto userResponseDto = userService.signup(requestDto);
        ApiResponse<UserResponseDto> response = ApiResponse.<UserResponseDto>builder()
                .msg(ResponseText.USER_SIGNUP_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(userResponseDto)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 사용자 로그인 API
     *
     * @param requestDto 사용자 로그인 요청 데이터
     * @return JWT 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDto>> login(@Valid @RequestBody UserLoginRequestDto requestDto) {
        JwtResponseDto jwtResponseDto = userService.login(requestDto);

        ApiResponse<JwtResponseDto> response = ApiResponse.<JwtResponseDto>builder()
                .msg(ResponseText.USER_LOGIN_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(jwtResponseDto)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 로그아웃 API
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestHeader("Authorization") String authHeader) {

        String accessToken = authHeader.substring(7);

        userService.logout(userDetails, accessToken);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .msg(ResponseText.LOGOUT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴 API
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @return 성공 메시지
     */
    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.deleteAccount(userDetails);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .msg(ResponseText.DELETE_ACCOUNT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(response);
    }
}
