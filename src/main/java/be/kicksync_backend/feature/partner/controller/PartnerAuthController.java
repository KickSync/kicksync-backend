package be.kicksync_backend.feature.partner.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.feature.partner.dto.PartnerSignupRequestDto;
import be.kicksync_backend.feature.partner.service.PartnerAuthService;
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

@Tag(name = "Partner Auth", description = "입점사 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner")
public class PartnerAuthController {

    private final PartnerAuthService partnerAuthService;

    @Operation(summary = "입점사 회원가입", description = "입점사 계정을 생성하고 파트너 정보를 등록합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 아이디")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> signup(@Valid @RequestBody PartnerSignupRequestDto requestDto) {
        UserResponseDto responseDto = partnerAuthService.signup(requestDto);
        ApiResponse<UserResponseDto> apiResponse = ApiResponse.<UserResponseDto>builder()
                .msg(ResponseText.USER_SIGNUP_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(responseDto)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
}
