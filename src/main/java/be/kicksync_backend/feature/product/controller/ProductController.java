package be.kicksync_backend.feature.product.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Product", description = "상품 조회 API")
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
    @Operation(summary = "전체 상품 조회", description = "모든 상품 목록을 페이지네이션하여 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    })
    @Parameters({
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
        @Parameter(name = "size", description = "페이지 크기", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20")),
        @Parameter(name = "sort", description = "정렬 기준 (예: id,desc)", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "id,desc"))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getAllProducts(
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "id") Pageable pageable) {
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
    @Operation(summary = "단일 상품 조회", description = "특정 상품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
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
