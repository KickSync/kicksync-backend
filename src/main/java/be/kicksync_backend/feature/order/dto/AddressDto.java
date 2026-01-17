package be.kicksync_backend.feature.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    @Schema(description = "우편번호", example = "04524")
    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipcode;

    @Schema(description = "기본 주소 (도로명)", example = "서울특별시 중구 세종대로 110")
    @NotBlank(message = "기본 주소는 필수입니다.")
    private String street;

    @Schema(description = "상세 주소", example = "5층")
    @NotBlank(message = "상세 주소는 필수입니다.")
    private String detail;
}