package be.kicksync_backend.feature.payment.util;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
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

    public IamportResponse<Payment> getPaymentInfoByImpUid(String impUid) throws IamportResponseException, IOException {
        return iamportClient.paymentByImpUid(impUid);
    }

    @Retryable(
            retryFor = {IamportResponseException.class, IOException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000),
            recover = "recoverCancelPayment"
    )
    public IamportResponse<Payment> cancelPaymentByImpUid(String impUid, String reason) throws IamportResponseException, IOException {
        CancelData cancelData = new CancelData(impUid, true);
        cancelData.setReason(reason);
        return iamportClient.cancelPaymentByImpUid(cancelData);
    }

    @Recover
    public IamportResponse<Payment> recoverCancelPayment(IamportResponseException e) {
        log.error("PortOne 결제 취소 최종 실패 - HTTP Status: {}, Message: {}", e.getHttpStatusCode(), e.getMessage());
        if (e.getMessage().contains("이미 취소된 거래")) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }

    @Recover
    public IamportResponse<Payment> recoverCancelPayment(IOException e) {
        log.error("PortOne 통신 중 IOException 발생, Exception: {}", e.getMessage());
        throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
    }
}
