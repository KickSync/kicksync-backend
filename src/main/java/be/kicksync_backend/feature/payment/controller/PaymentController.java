package be.kicksync_backend.feature.payment.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.payment.dto.PaymentCancelRequestDto;
import be.kicksync_backend.feature.payment.dto.PaymentRequestDto;
import be.kicksync_backend.feature.payment.dto.PaymentResponseDto;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.service.PaymentService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 검증 API
     *
     * @param requestDto 결제 요청 데이터
     * @return 검증된 결제 정보
     */
    @PostMapping("/portone")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(@RequestBody PaymentRequestDto requestDto) throws IOException, IamportResponseException {
        Payment payment = paymentService.verifyPayment(requestDto);
        ApiResponse<Payment> apiResponse = ApiResponse.<Payment>builder()
                .msg(ResponseText.PAYMENT_VERIFICATION_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(payment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 주문 ID로 결제 내역 조회 API
     *
     * @param orderId 주문 ID
     * @return 결제 내역
     *
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByOrderId(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        ApiResponse<PaymentResponseDto> apiResponse = ApiResponse.<PaymentResponseDto>builder()
                .msg(ResponseText.PAYMENT_FOUND_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(PaymentResponseDto.from(payment))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 내 결제 내역 조회 API
     *
     * @param userDetails 현재 인증된 사용자 정보
     * @return 사용자의 결제 내역 목록
     */
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<PaymentResponseDto>>> getMyPayments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Payment> payments = paymentService.getMyPayments(userDetails.getUser().getId());
        ApiResponse<List<PaymentResponseDto>> apiResponse = ApiResponse.<List<PaymentResponseDto>>builder()
                .msg(ResponseText.PAYMENT_HISTORY_FOUND_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(PaymentResponseDto.from(payments))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 결제 취소 API
     *
     * @param cancelDto   결제 취소 요청 데이터
     * @param userDetails 현재 인증된 사용자 정보
     * @return 취소된 결제 정보
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Payment>> cancelPayment(
            @RequestBody PaymentCancelRequestDto cancelDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException, IamportResponseException {
        Payment payment = paymentService.cancelPayment(cancelDto, userDetails.getUser().getId());
        ApiResponse<Payment> apiResponse = ApiResponse.<Payment>builder()
                .msg(ResponseText.PAYMENT_CANCEL_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(payment)
                .build();
        return ResponseEntity.ok(apiResponse);
    }


}