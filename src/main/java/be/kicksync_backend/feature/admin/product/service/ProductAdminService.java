package be.kicksync_backend.feature.admin.product.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.repository.OrderItemRepository;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.DropEventRepository;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.partner.repository.PartnerRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductAdminService {
    private final ProductRepository productRepository;
    private final ProductQueryService productQueryService;
    private final OrderItemRepository orderItemRepository;
    private final DropEventRepository dropEventRepository;
    private final PartnerRepository partnerRepository;

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto) {
        Partner partner = partnerRepository.findById(requestDto.getPartnerId())
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNER_NOT_FOUND));
        
        Product product = requestDto.toEntity(partner);
        Product savedProduct = productRepository.save(product);
        return new ProductResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productQueryService.getAllProducts(pageable);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long productId) {
        return productQueryService.getProduct(productId);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        product.update(
                requestDto.getName(), 
                requestDto.getModel(), 
                requestDto.getReleaseDate(), 
                requestDto.getRetailPrice(),
                requestDto.getStock()
        );
        return new ProductResponseDto(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (orderItemRepository.existsByProductId(productId) || dropEventRepository.existsByProductId(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_IN_USE);
        }

        productRepository.delete(product);
    }
}