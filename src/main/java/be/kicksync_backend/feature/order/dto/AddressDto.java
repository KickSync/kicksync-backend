package be.kicksync_backend.feature.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipcode;

    @NotBlank(message = "기본 주소는 필수입니다.")
    private String street;

    @NotBlank(message = "상세 주소는 필수입니다.")
    private String detail;
}