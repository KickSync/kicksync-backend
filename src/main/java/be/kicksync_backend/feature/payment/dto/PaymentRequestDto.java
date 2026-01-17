package be.kicksync_backend.feature.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "주문 ID", example = "1")
    @NotNull(message = "주문 ID는 필수입니다.")
    @Positive(message = "주문 ID는 양수여야 합니다.")
    private Long orderId;

    @Schema(description = "PortOne 결제 고유 ID", example = "imp_1234567890")
    @NotBlank(message = "PortOne 결제 고유 ID는 필수입니다.")
    @JsonProperty("imp_uid")
    private String impUid;       // imp_uid

    @Schema(description = "결제 수단", example = "card")
    @NotBlank(message = "결제 수단은 필수입니다.")
    @JsonProperty("pay_method")
    private String payMethod;    // pay_method

    @Schema(description = "주문 번호", example = "merchant_1234567890")
    @NotBlank(message = "주문 번호는 필수입니다.")
    @JsonProperty("merchant_uid")
    private String merchantUid;  // merchant_uid

    @Schema(description = "결제 금액", example = "150000")
    @Positive(message = "결제 금액은 양수여야 합니다.")
    @JsonProperty("paid_amount")
    private BigDecimal paidAmount;      // paid_amount

    @Schema(description = "PG사", example = "kcp")
    @NotBlank(message = "PG사는 필수입니다.")
    @JsonProperty("pg_provider")
    private String pgProvider;   // pg_provider

    @Schema(description = "PG 타입", example = "payment")
    @NotBlank(message = "PG 타입은 필수입니다.")
    @JsonProperty("pg_type")
    private String pgType;       // pg_type

    @Schema(description = "PG 거래 ID", example = "t_1234567890")
    @NotBlank(message = "PG 거래 ID는 필수입니다.")
    @JsonProperty("pg_tid")
    private String pgTid;        // pg_tid

    @Schema(description = "결제 상태", example = "paid")
    @NotBlank(message = "결제 상태는 필수입니다.")
    private String status;       // status

    @Schema(description = "카드 이름", example = "신한카드")
    @NotBlank(message = "카드 이름은 필수입니다.")
    @JsonProperty("card_name")
    private String cardName;     // card_name

    @Schema(description = "카드 번호", example = "1234-5678-****-****")
    @NotBlank(message = "카드 번호는 필수입니다.")
    @JsonProperty("card_number")
    private String cardNumber;   // card_number
}
