package be.kicksync_backend.feature.admin.product.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductAdminService {
    private final ProductRepository productRepository;

    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {
        Product product = requestDto.toEntity();
        Product savedProduct = productRepository.save(product);
        return new ProductResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return new ProductResponseDto(product);
    }

    public ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        product.update(requestDto.getName(), requestDto.getModel(), requestDto.getReleaseDate(), requestDto.getRetailPrice());
        return new ProductResponseDto(product);
    }

    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        productRepository.delete(product);
    }
} 