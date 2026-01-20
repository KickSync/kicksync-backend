package be.kicksync_backend.feature.admin.product.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.feature.admin.product.service.ProductAdminService;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Product", description = "관리자 상품 관리 API")
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
    @Operation(summary = "상품 생성", description = "관리자가 새로운 상품을 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "상품 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
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
    @Operation(summary = "전체 상품 조회", description = "관리자가 전체 상품 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공")
    })
    @Parameters({
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
        @Parameter(name = "size", description = "페이지 크기", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "20")),
        @Parameter(name = "sort", description = "정렬 기준 (예: createdAt,desc)", in = ParameterIn.QUERY, schema = @Schema(type = "string", defaultValue = "createdAt,desc"))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDto>>> getAllProducts(
            @Parameter(hidden = true) @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<ProductResponseDto> products = productAdminService.getAllProducts(pageable);
        ApiResponse<Page<ProductResponseDto>> response = ApiResponse.<Page<ProductResponseDto>>builder()
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
    @Operation(summary = "단일 상품 조회", description = "관리자가 특정 상품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
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
    @Operation(summary = "상품 정보 수정", description = "관리자가 상품 정보를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음")
    })
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
    @Operation(summary = "상품 삭제", description = "관리자가 상품을 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 상품은 삭제 불가")
    })
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