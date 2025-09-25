package be.kicksync_backend.feature.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateRequestDto {

    @Valid
    @NotNull(message = "주문 상품 목록은 필수입니다.")
    private List<OrderItemRequestDto> orderItems;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String receiverName;

    @NotBlank(message = "수령인 전화번호는 필수입니다.")
    private String receiverPhone;

    @Valid
    @NotNull(message = "주소는 필수입니다.")
    private AddressDto address;

    private String requestMessage;
}
