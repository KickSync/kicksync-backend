package be.kicksync_backend.feature.order.repository;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByProductId(Long productId);

    Page<Order> findByUser(User user, Pageable pageable);
}