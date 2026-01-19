package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.entity.OrderStatus;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.dto.PaymentRequestDto;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.payment.util.PaymentClient;
import be.kicksync_backend.feature.user.entity.User;
import com.siot.IamportRestClient.response.IamportResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PaymentTransactionService paymentTransactionService;

    @Test
    @DisplayName("결제 검증 시 이미 PAID 상태인 결제 내역이 있으면 성공(멱등성)")
    void verifyPayment_Idempotency_Success() throws Exception {
        // given
        String impUid = "imp_123456";
        Long userId = 1L;
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .impUid(impUid)
                .orderId(1L)
                .build();

        Payment existingPayment = mock(Payment.class);
        given(existingPayment.getStatus()).willReturn(PaymentStatus.PAID);

        given(paymentRepository.findByImpUid(impUid)).willReturn(Optional.of(existingPayment));

        // when
        Payment result = paymentService.verifyPayment(requestDto, userId);

        // then
        assertThat(result).isEqualTo(existingPayment);
        verify(paymentClient, never()).getPaymentInfoByImpUid(any());
    }

    @Test
    @DisplayName("결제 검증 시 이미 취소된(CANCELLED) 상태라면 에러 발생")
    void verifyPayment_AlreadyCancelled_ThrowsException() {
        // given
        String impUid = "imp_123456";
        Long userId = 1L;
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .impUid(impUid)
                .orderId(1L)
                .build();

        Payment existingPayment = mock(Payment.class);
        given(existingPayment.getStatus()).willReturn(PaymentStatus.CANCELLED);

        given(paymentRepository.findByImpUid(impUid)).willReturn(Optional.of(existingPayment));

        // when & then
        assertThatThrownBy(() -> paymentService.verifyPayment(requestDto, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_ALREADY_CANCELLED);
    }

    @Test
    @DisplayName("결제 검증 성공: 결제 금액과 주문 금액 일치 및 상태 정상")
    void verifyPayment_Success() throws Exception {
        // given
        String impUid = "imp_123456";
        String merchantUid = "merchant_123456";
        Long userId = 1L;
        BigDecimal amount = BigDecimal.valueOf(10000);
        
        PaymentRequestDto requestDto = PaymentRequestDto.builder()
                .impUid(impUid)
                .merchantUid(merchantUid)
                .build();

        // 1. Repo: 기존 결제 내역 없음
        given(paymentRepository.findByImpUid(impUid)).willReturn(Optional.empty());

        // 2. Client: 유효한 결제 정보 반환
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = mock(IamportResponse.class);
        com.siot.IamportRestClient.response.Payment paymentInfo = mock(com.siot.IamportRestClient.response.Payment.class);
        
        given(paymentClient.getPaymentInfoByImpUid(impUid)).willReturn(iamportResponse);
        given(iamportResponse.getResponse()).willReturn(paymentInfo);
        
        given(paymentInfo.getAmount()).willReturn(amount);
        given(paymentInfo.getMerchantUid()).willReturn(merchantUid);
        given(paymentInfo.getStatus()).willReturn("paid");

        // 3. Order Validation
        Order order = mock(Order.class);
        User user = mock(User.class);
        
        given(orderRepository.findAllByMerchantUid(merchantUid)).willReturn(java.util.List.of(order));
        given(order.getUser()).willReturn(user);
        given(user.getId()).willReturn(userId);
        given(order.getStatus()).willReturn(OrderStatus.PENDING_PAYMENT);
        given(order.getFinalPrice()).willReturn(amount);

        // 4. Transaction Service
        Payment payment = mock(Payment.class);
        given(paymentTransactionService.completePaymentVerification(eq(paymentInfo), anyList())).willReturn(payment);

        // when
        Payment result = paymentService.verifyPayment(requestDto, userId);

        // then
        assertThat(result).isEqualTo(payment);
        verify(paymentTransactionService).completePaymentVerification(eq(paymentInfo), anyList());
    }
}
