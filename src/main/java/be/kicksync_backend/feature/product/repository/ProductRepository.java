package be.kicksync_backend.feature.product.repository;

import be.kicksync_backend.feature.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
} 
 