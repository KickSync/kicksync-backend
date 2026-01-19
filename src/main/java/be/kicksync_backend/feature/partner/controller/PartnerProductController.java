package be.kicksync_backend.feature.partner.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.partner.service.PartnerProductService;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Partner Product", description = "입점사 상품 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partner/products")
public class PartnerProductController {

    private final PartnerProductService partnerProductService;

    /**
     * 입점사: 상품 등록 API
     *
     * @param requestDto 상품 생성 요청 데이터
     * @param userDetails 인증된 입점사 사용자 정보
     * @return 생성된 상품 정보
     */
    @Operation(summary = "상품 등록", description = "입점사가 새로운 상품을 등록합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(
            @Valid @RequestBody ProductCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProductResponseDto product = partnerProductService.createProduct(requestDto, userDetails.getUser());
        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .msg(ResponseText.CREATE_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(product)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 입점사: 내 상품 목록 조회 API
     *
     * @param pageable 페이징 정보
     * @param userDetails 인증된 입점사 사용자 정보
     * @return 입점사가 등록한 상품 목록
     */
    @Operation(summary = "내 상품 목록 조회", description = "입점사가 등록한 상품 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getMyProducts(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<ProductResponseDto> products = partnerProductService.getMyProducts(pageable, userDetails.getUser());
        ApiResponse<Page<ProductResponseDto>> response = ApiResponse.<Page<ProductResponseDto>>builder()
                .msg(ResponseText.GET_PRODUCTS_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(products)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 입점사: 상품 정보 수정 API
     *
     * @param productId 수정할 상품 ID
     * @param requestDto 수정할 상품 정보
     * @param userDetails 인증된 입점사 사용자 정보
     * @return 수정된 상품 정보
     */
    @Operation(summary = "상품 수정", description = "입점사가 자신의 상품 정보를 수정합니다.")
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        ProductResponseDto updatedProduct = partnerProductService.updateProduct(productId, requestDto, userDetails.getUser());
        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .msg(ResponseText.UPDATE_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(updatedProduct)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 입점사: 상품 삭제 API
     *
     * @param productId 삭제할 상품 ID
     * @param userDetails 인증된 입점사 사용자 정보
     * @return 삭제 완료 메시지
     */
    @Operation(summary = "상품 삭제", description = "입점사가 자신의 상품을 삭제합니다.")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        partnerProductService.deleteProduct(productId, userDetails.getUser());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .msg(ResponseText.DELETE_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(response);
    }
}
