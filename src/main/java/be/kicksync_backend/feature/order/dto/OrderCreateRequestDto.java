package be.kicksync_backend.feature.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDto {

    @Schema(description = "주문 상품 목록")
    @Valid
    @NotNull(message = "주문 상품 목록은 필수입니다.")
    private List<OrderItemRequestDto> orderItems;

    @Schema(description = "수령인 이름", example = "홍길동")
    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String receiverName;

    @Schema(description = "수령인 전화번호", example = "010-1234-5678")
    @NotBlank(message = "수령인 전화번호는 필수입니다.")
    private String receiverPhone;

    @Schema(description = "배송지 주소")
    @Valid
    @NotNull(message = "주소는 필수입니다.")
    private AddressDto address;

    @Schema(description = "배송 요청 사항", example = "문 앞에 놔주세요.")
    private String requestMessage;
}
