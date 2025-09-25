package be.kicksync_backend.feature.order.repository;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);

    List<Order> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
}