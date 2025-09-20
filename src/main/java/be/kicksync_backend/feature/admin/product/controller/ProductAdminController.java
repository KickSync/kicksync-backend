package be.kicksync_backend.feature.admin.product.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.feature.admin.product.service.ProductAdminService;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
public class ProductAdminController {
    private final ProductAdminService productAdminService;

    /**
     * 관리자: 상품 생성 API
     *
     * @param requestDto 상품 생성 요청 데이터
     * @return 생성된 상품 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(@Valid @RequestBody ProductCreateRequestDto requestDto) {
        ProductResponseDto product = productAdminService.createProduct(requestDto);
        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .msg(ResponseText.CREATE_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(product)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 관리자: 전체 상품 조회 API
     *
     * @return 모든 상품 정보 리스트
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> getAllProducts() {
        List<ProductResponseDto> products = productAdminService.getAllProducts();
        ApiResponse<List<ProductResponseDto>> response = ApiResponse.<List<ProductResponseDto>>builder()
                .msg(ResponseText.GET_PRODUCTS_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(products)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자: 단일 상품 조회 API
     *
     * @param productId 조회할 상품의 ID
     * @return 단일 상품 정보
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable Long productId) {
        ProductResponseDto product = productAdminService.getProduct(productId);
        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .msg(ResponseText.GET_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(product)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자: 상품 정보 수정 API
     *
     * @param productId  수정할 상품의 ID
     * @param requestDto 수정할 상품 정보
     * @return 수정된 상품 정보
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(@PathVariable Long productId, @Valid @RequestBody ProductUpdateRequestDto requestDto) {
        ProductResponseDto updatedProduct = productAdminService.updateProduct(productId, requestDto);
        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .msg(ResponseText.UPDATE_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(updatedProduct)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 관리자: 상품 삭제 API
     *
     * @param productId 삭제할 상품의 ID
     * @return 성공 응답
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId) {
        productAdminService.deleteProduct(productId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .msg(ResponseText.DELETE_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(response);
    }
}