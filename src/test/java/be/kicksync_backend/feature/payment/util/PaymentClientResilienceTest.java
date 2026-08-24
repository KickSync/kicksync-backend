package be.kicksync_backend.feature.payment.util;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import com.siot.IamportRestClient.IamportClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("local")
public class PaymentClientResilienceTest {

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    private IamportClient mockIamportClient;

    @BeforeEach
    void setUp() {
        mockIamportClient = mock(IamportClient.class);
        ReflectionTestUtils.setField(paymentClient, "iamportClient", mockIamportClient);
        
        // Reset registries to avoid state leakage between tests
        circuitBreakerRegistry.circuitBreaker("paymentReadClient").reset();
        circuitBreakerRegistry.circuitBreaker("paymentWriteClient").reset();
    }

    @Test
    @DisplayName("Read 서킷 브레이커: 5번 연속 실패 시 Read 서킷이 OPEN되고 후속 요청은 즉시 Fail-Fast 차단한다")
    void testReadCircuitBreakerOpenOnPersistentFailures() throws Exception {
        // given
        when(mockIamportClient.paymentByImpUid(anyString())).thenThrow(new IOException("Connection timed out"));

        // when & then - 5번 연속 실패 시 sliding window(10)에서 minimumNumberOfCalls(5) 충족하여 서킷 오픈 임계치(50%) 도달
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> paymentClient.getPaymentInfoByImpUid("imp_123"))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        // Read 서킷 브레이커 상태가 OPEN으로 변경되었는지 검증
        CircuitBreaker readCircuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentReadClient");
        assertThat(readCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Read 서킷 오픈 상태에서 요청 시 MockIamportClient 호출 없이 즉시 Fail-Fast 차단 (fallback 적용)
        assertThatThrownBy(() -> paymentClient.getPaymentInfoByImpUid("imp_123"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_VERIFICATION_FAILED);

        // Read 서킷이 OPEN되어도 Write 서킷(paymentWriteClient)은 CLOSED 상태를 유지하여 스코프가 격리됨을 검증
        CircuitBreaker writeCircuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentWriteClient");
        assertThat(writeCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        // 실제 mockIamportClient는 리트라이 포함 최대 15번까지만 호출되어야 하고 서킷 오픈 후엔 호출 안됨
        verify(mockIamportClient, atMost(15)).paymentByImpUid(anyString());
    }
}
