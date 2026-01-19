package be.kicksync_backend.feature.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PartnerSignupRequestDto {

    @Schema(description = "아이디 (반드시 'pt_'로 시작해야 함)", example = "pt_partner1")
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 7, max = 15, message = "아이디는 7자 이상 15자 이하이어야 합니다.")
    @Pattern(regexp = "^pt_[a-z0-9]+$", message = "아이디는 'pt_'로 시작해야 하며, 소문자와 숫자만 포함할 수 있습니다.")
    private String username;

    @Schema(description = "비밀번호", example = "Password123!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상 15자 이하이어야 합니다.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", 
             message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @Schema(description = "파트너사 이름", example = "Nike Official")
    @NotBlank(message = "파트너사 이름은 필수입니다.")
    private String partnerName;

    @Schema(description = "사업자 등록번호", example = "123-45-67890")
    @NotBlank(message = "사업자 등록번호는 필수입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 등록번호 형식이 올바르지 않습니다. (000-00-00000)")
    private String businessNumber;
}
