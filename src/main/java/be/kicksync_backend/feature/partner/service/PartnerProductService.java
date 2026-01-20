package be.kicksync_backend.feature.partner.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.repository.OrderItemRepository;
import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.partner.repository.PartnerRepository;
import be.kicksync_backend.feature.product.dto.ProductCreateRequestDto;
import be.kicksync_backend.feature.product.dto.ProductResponseDto;
import be.kicksync_backend.feature.product.dto.ProductUpdateRequestDto;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.DropEventRepository;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerProductService {

    private final ProductRepository productRepository;
    private final PartnerRepository partnerRepository;
    private final OrderItemRepository orderItemRepository;
    private final DropEventRepository dropEventRepository;

    private Partner getPartnerForUser(Long userId) {
        return partnerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTNER_NOT_FOUND));
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDto createProduct(ProductCreateRequestDto requestDto, Long userId) {
        Partner partner = getPartnerForUser(userId);

        Product product = Product.builder()
                .name(requestDto.getName())
                .model(requestDto.getModel())
                .releaseDate(requestDto.getReleaseDate())
                .retailPrice(requestDto.getRetailPrice())
                .stock(requestDto.getStock())
                .partner(partner)
                .build();
                
        Product savedProduct = productRepository.save(product);
        return new ProductResponseDto(savedProduct);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getMyProducts(Pageable pageable, Long userId) {
        Partner partner = getPartnerForUser(userId);
        return productRepository.findAllByPartnerId(partner.getId(), pageable)
                .map(ProductResponseDto::new);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto requestDto, Long userId) {
        Partner partner = getPartnerForUser(userId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getPartner().getId().equals(partner.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        product.update(requestDto.getName(), requestDto.getModel(), requestDto.getReleaseDate(), requestDto.getRetailPrice());
        return new ProductResponseDto(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long productId, Long userId) {
        Partner partner = getPartnerForUser(userId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.getPartner().getId().equals(partner.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (orderItemRepository.existsByProductId(productId) || dropEventRepository.existsByProductId(productId)) {
            throw new CustomException(ErrorCode.PRODUCT_IN_USE);
        }

        productRepository.delete(product);
    }
}
