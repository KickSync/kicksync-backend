package be.kicksync_backend.feature.product.service;

import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductQueryService productQueryService;

    @Cacheable(value = "products", key = "'allProducts-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productQueryService.getAllProducts(pageable);
    }

    @Cacheable(value = "products", key = "#productId")
    public ProductResponseDto getProduct(Long productId) {
        return productQueryService.getProduct(productId);
    }
}
