package be.kicksync_backend.feature.order.repository;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser(User user, Pageable pageable);

    List<Order> findAllByMerchantUid(String merchantUid);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findAllWithItemsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);
}