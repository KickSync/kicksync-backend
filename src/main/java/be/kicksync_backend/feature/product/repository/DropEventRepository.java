package be.kicksync_backend.feature.product.repository;

import be.kicksync_backend.feature.product.entity.DropEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DropEventRepository extends JpaRepository<DropEvent, Long> {
    boolean existsByProductId(Long productId);
}