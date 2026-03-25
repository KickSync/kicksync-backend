package be.kicksync_backend.feature.payment.util;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;

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

    @Retryable(
            retryFor = {IamportResponseException.class, IOException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public IamportResponse<Payment> getPaymentInfoByImpUid(String impUid) throws IamportResponseException, IOException {
        return iamportClient.paymentByImpUid(impUid);
    }

    @Retryable(
            retryFor = {IamportResponseException.class, IOException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public IamportResponse<Payment> cancelPaymentByImpUid(String impUid, java.math.BigDecimal amount, String reason, java.math.BigDecimal checksum) throws IamportResponseException, IOException {
        CancelData cancelData = new CancelData(impUid, true, amount);
        cancelData.setReason(reason);
        cancelData.setChecksum(checksum);
        return iamportClient.cancelPaymentByImpUid(cancelData);
    }

    @Recover
    public IamportResponse<Payment> recoverGetPaymentInfo(Exception e, String impUid) {
        log.error("PortOne 결제 정보 조회 최종 실패 - impUid: {}, Exception: {}", impUid, e.getMessage());
        throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
    }

    @Recover
    public IamportResponse<Payment> recoverCancelPayment(IamportResponseException e, String impUid, String reason) {
        log.error("PortOne 결제 취소 최종 실패 - HTTP Status: {}, Message: {}", e.getHttpStatusCode(), e.getMessage());
        if (e.getMessage() != null && e.getMessage().contains("이미 취소된 거래")) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    @Recover
    public IamportResponse<Payment> recoverCancelPayment(IOException e, String impUid, String reason) {
        log.error("PortOne 통신 중 IOException 발생 - impUid: {}, Exception: {}", impUid, e.getMessage());
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }
}
