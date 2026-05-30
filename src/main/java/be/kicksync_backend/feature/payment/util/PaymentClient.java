package be.kicksync_backend.feature.payment.util;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@Service
public class PaymentClient {
    private IamportClient iamportClient;

    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, apiSecret);
    }

    @CircuitBreaker(name = "paymentClient", fallbackMethod = "fallbackGetPaymentInfo")
    @Retry(name = "paymentClient")
    public IamportResponse<Payment> getPaymentInfoByImpUid(String impUid) throws IamportResponseException, IOException {
        return iamportClient.paymentByImpUid(impUid);
    }

    @CircuitBreaker(name = "paymentClient", fallbackMethod = "fallbackCancelPayment")
    @Retry(name = "paymentClient")
    public IamportResponse<Payment> cancelPaymentByImpUid(String impUid, BigDecimal amount, String reason, BigDecimal checksum) throws IamportResponseException, IOException {
        CancelData cancelData = new CancelData(impUid, true, amount);
        cancelData.setReason(reason);
        cancelData.setChecksum(checksum);
        return iamportClient.cancelPaymentByImpUid(cancelData);
    }

    // --- Fallbacks ---
    public IamportResponse<Payment> fallbackGetPaymentInfo(String impUid, Throwable t) {
        log.error("PortOne 결제 정보 조회 최종 실패 - impUid: {}, Exception: {}", impUid, t.getMessage());
        throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
    }

    public IamportResponse<Payment> fallbackCancelPayment(String impUid, BigDecimal amount, String reason, BigDecimal checksum, IamportResponseException e) {
        log.error("PortOne 결제 취소 최종 실패 - HTTP Status: {}, Message: {}", e.getHttpStatusCode(), e.getMessage());
        if (e.getMessage() != null && e.getMessage().contains("이미 취소된 거래")) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    public IamportResponse<Payment> fallbackCancelPayment(String impUid, BigDecimal amount, String reason, BigDecimal checksum, IOException e) {
        log.error("PortOne 통신 중 IOException 발생 - impUid: {}, Exception: {}", impUid, e.getMessage());
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    public IamportResponse<Payment> fallbackCancelPayment(String impUid, BigDecimal amount, String reason, BigDecimal checksum, Throwable t) {
        log.error("PortOne 결제 취소 중 기타 예외 발생 - impUid: {}, Exception: {}", impUid, t.getMessage());
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }
}
