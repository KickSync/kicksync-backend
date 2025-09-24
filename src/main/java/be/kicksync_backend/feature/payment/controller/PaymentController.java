package be.kicksync_backend.feature.payment.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.service.OrderService;
import be.kicksync_backend.feature.payment.dto.PaymentRequestDto;
import be.kicksync_backend.feature.payment.dto.PaymentResponseDto;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.service.PaymentService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    /**
     * 결제 검증 API
     *
     * @param requestDto  결제 요청 데이터
     * @param userDetails 현재 인증된 사용자 정보
     * @return 검증된 결제 정보
     */
    @PostMapping("/portone")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> verifyPayment(
            @Valid @RequestBody PaymentRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException, IamportResponseException {
        Payment payment = paymentService.verifyPayment(requestDto, userDetails.getUser().getId());
        ApiResponse<PaymentResponseDto> apiResponse = ApiResponse.<PaymentResponseDto>builder()
                .msg(ResponseText.PAYMENT_VERIFICATION_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(PaymentResponseDto.from(payment))
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 주문 ID로 결제 내역 조회 API
     *
     * @param orderId     주문 ID
     * @param userDetails 현재 인증된 사용자 정보
     * @return 결제 내역
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentByOrderId(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Payment payment = paymentService.getPaymentByOrderId(orderId, userDetails.getUser().getId());
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
     * 주문 취소 API
     *
     * @param orderId     취소할 주문 ID
     * @param cancelDto   결제 취소 요청 데이터
     * @param userDetails 현재 인증된 사용자 정보
     * @return 취소된 결제 정보
     */
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelPayment(
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderCancelRequestDto cancelDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException, IamportResponseException {
        orderService.cancelOrder(orderId, cancelDto, userDetails.getUser().getId());
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .msg(ResponseText.PAYMENT_CANCEL_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
