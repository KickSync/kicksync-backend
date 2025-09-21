package be.kicksync_backend.feature.product.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 사용자: 전체 상품 조회 API
     *
     * @return 모든 상품 정보 리스트
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getAllProducts(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<ProductResponseDto> products = productService.getAllProducts(pageable);
        ApiResponse<Page<ProductResponseDto>> response = ApiResponse.<Page<ProductResponseDto>>builder()
                .msg(ResponseText.GET_PRODUCTS_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(products)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자: 단일 상품 조회 API
     *
     * @param productId 조회할 상품의 ID
     * @return 단일 상품 정보
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable Long productId) {
        ProductResponseDto product = productService.getProduct(productId);
        ApiResponse<ProductResponseDto> response = ApiResponse.<ProductResponseDto>builder()
                .msg(ResponseText.GET_PRODUCT_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(product)
                .build();
        return ResponseEntity.ok(response);
    }
}
