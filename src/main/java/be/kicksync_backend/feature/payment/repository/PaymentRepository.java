package be.kicksync_backend.feature.payment.repository;

import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.payment.dto.PartnerSettlementDto;
import be.kicksync_backend.feature.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_IdAndUserId(Long orderId, Long userId);

    List<Payment> findAllByUserId(Long userId);

    Optional<Payment> findByImpUid(String impUid);

    Optional<Payment> findByOrder_Id(Long orderId);

    @Query("SELECT p.partnerId as partnerId, SUM(p.paymentAmount) as totalAmount " +
            "FROM Payment p " +
            "WHERE p.status = :status AND p.paymentDate BETWEEN :start AND :end AND p.partnerId IS NOT NULL " +
            "GROUP BY p.partnerId")
    List<PartnerSettlementDto> findPartnerTotalsByStatusAndPaymentDateBetween(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
