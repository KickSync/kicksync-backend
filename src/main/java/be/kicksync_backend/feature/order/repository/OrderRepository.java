package be.kicksync_backend.feature.order.repository;

import be.kicksync_backend.feature.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface OrderRepository extends JpaRepository<Order, Long> {
} 