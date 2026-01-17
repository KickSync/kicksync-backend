package be.kicksync_backend.feature.user.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.user.dto.ProfileUpdateRequestDto;
import be.kicksync_backend.feature.user.dto.UserProfileResponseDto;
import be.kicksync_backend.feature.user.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MyPage", description = "마이페이지 API")
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 내 프로필 조회 API
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @return 사용자 프로필 정보
     */
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공")
    })
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserProfileResponseDto profile = myPageService.getProfile(userDetails.getUsername());
        ApiResponse<UserProfileResponseDto> response = ApiResponse.<UserProfileResponseDto>builder()
                .msg(ResponseText.PROFILE_GET_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(profile)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 내 프로필 수정 API
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @param requestDto  프로필 수정 요청 데이터
     * @return 성공 메시지
     */
    @Operation(summary = "내 프로필 수정", description = "로그인한 사용자의 프로필(닉네임 등)을 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @Valid @RequestBody ProfileUpdateRequestDto requestDto) {
        myPageService.updateProfile(userDetails.getUsername(), requestDto);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .msg(ResponseText.PROFILE_UPDATE_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 내 주문 내역 조회 API
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @return 사용자 주문 내역 목록
     */
    @Operation(summary = "내 주문 내역 조회", description = "로그인한 사용자의 주문 내역을 페이징 처리하여 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "주문 내역 조회 성공")
    })
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getMyOrderHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderResponseDto> orderHistory = myPageService.getOrderHistory(userDetails.getUsername(), pageable);
        ApiResponse<Page<OrderResponseDto>> response = ApiResponse.<Page<OrderResponseDto>>builder()
                .msg(ResponseText.ORDER_HISTORY_GET_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(orderHistory)
                .build();
        return ResponseEntity.ok(response);
    }
}
