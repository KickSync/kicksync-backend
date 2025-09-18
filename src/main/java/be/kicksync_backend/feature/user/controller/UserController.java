package be.kicksync_backend.feature.user.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.feature.user.dto.UserLoginRequestDto;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.dto.UserSignupRequestDto;
import be.kicksync_backend.feature.user.service.UserService;
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
     * Create a new user account.
     *
     * Accepts a UserSignupRequestDto and returns a ResponseEntity containing an ApiResponse
     * with the created UserResponseDto payload and HTTP status 201 (Created).
     *
     * @param requestDto the signup request data
     * @return ResponseEntity wrapping ApiResponse<UserResponseDto> with the created user and HTTP 201
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(@RequestBody UserSignupRequestDto requestDto) {
        UserResponseDto userResponseDto = userService.signup(requestDto);
        ApiResponse<UserResponseDto> response = ApiResponse.<UserResponseDto>builder()
                .msg(ResponseText.USER_SIGNUP_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(userResponseDto)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and returns a JWT wrapped in a standardized ApiResponse.
     *
     * @param requestDto credentials and authentication data for the user login request
     * @return a ResponseEntity containing an ApiResponse with a JwtResponseDto on successful authentication
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponseDto>> login(@RequestBody UserLoginRequestDto requestDto) {
        JwtResponseDto jwtResponseDto = userService.login(requestDto);

        ApiResponse<JwtResponseDto> response = ApiResponse.<JwtResponseDto>builder()
                .msg(ResponseText.USER_LOGIN_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(jwtResponseDto)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Logs out the currently authenticated user.
     *
     * Calls the service to perform logout and returns a 200 OK ApiResponse containing a success message.
     *
     * @param userDetails the authenticated user's details (injected via {@code @AuthenticationPrincipal})
     * @return ResponseEntity containing an ApiResponse with no data and HTTP 200 status
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.logout(userDetails);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .msg(ResponseText.LOGOUT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Delete the authenticated user's account.
     *
     * Performs account removal for the currently authenticated principal and returns a standardized ApiResponse with no data.
     *
     * @param userDetails the authenticated user's principal (UserDetailsImpl)
     * @return ResponseEntity containing an ApiResponse<Void> with a success message and HTTP 200 OK
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
