package be.kicksync_backend.feature.payment.repository;

import be.kicksync_backend.feature.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findAllByUserId(Long userId);

    Optional<Payment> findByImpUid(String impUid);

}