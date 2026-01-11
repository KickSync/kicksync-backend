package be.kicksync_backend.feature.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    @NotNull(message = "주문 ID는 필수입니다.")
    @Positive(message = "주문 ID는 양수여야 합니다.")
    private Long orderId;

    @NotBlank(message = "PortOne 결제 고유 ID는 필수입니다.")
    @JsonProperty("imp_uid")
    private String impUid;       // imp_uid

    @NotBlank(message = "결제 수단은 필수입니다.")
    @JsonProperty("pay_method")
    private String payMethod;    // pay_method

    @NotBlank(message = "주문 번호는 필수입니다.")
    @JsonProperty("merchant_uid")
    private String merchantUid;  // merchant_uid

    @Positive(message = "결제 금액은 양수여야 합니다.")
    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;      // paid_amount

    @NotBlank(message = "PG사는 필수입니다.")
    @JsonProperty("pg_provider")
    private String pgProvider;   // pg_provider

    @NotBlank(message = "PG 타입은 필수입니다.")
    @JsonProperty("pg_type")
    private String pgType;       // pg_type

    @NotBlank(message = "PG 거래 ID는 필수입니다.")
    @JsonProperty("pg_tid")
    private String pgTid;        // pg_tid

    @NotBlank(message = "결제 상태는 필수입니다.")
    private String status;       // status

    @NotBlank(message = "카드 이름은 필수입니다.")
    @JsonProperty("card_name")
    private String cardName;     // card_name

    @NotBlank(message = "카드 번호는 필수입니다.")
    @JsonProperty("card_number")
    private String cardNumber;   // card_number
}
