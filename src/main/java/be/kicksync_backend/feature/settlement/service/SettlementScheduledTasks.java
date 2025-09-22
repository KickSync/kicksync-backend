package be.kicksync_backend.feature.settlement.service;

import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.settlement.entity.Settlement;
import be.kicksync_backend.feature.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduledTasks {
    private final PaymentRepository paymentRepository;
    private final SettlementRepository settlementRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @SchedulerLock(name = "dailySettlementTask")
    public void dailySettlement() {
        log.info("Daily settlement batch job started.");

        LocalDate settlementDate = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = settlementDate.atStartOfDay();
        LocalDateTime endOfDay = settlementDate.atTime(LocalTime.MAX);

        List<Payment> payments = paymentRepository.findAllByStatusAndCreatedAtBetween("paid", startOfDay, endOfDay);

        if (payments.isEmpty()) {
            log.info("No payments to settle for date: {}", settlementDate);
            return;
        }

        Map<Long, BigDecimal> partnerTotalAmounts = payments.stream()
                .filter(p -> p.getPartnerId() != null)
                .collect(Collectors.groupingBy(
                        Payment::getPartnerId,
                        Collectors.mapping(Payment::getPaymentAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        List<Settlement> settlementsToSave = partnerTotalAmounts.entrySet().stream()
                .map(entry -> Settlement.builder()
                        .partnerId(entry.getKey())
                        .totalAmount(entry.getValue())
                        .paymentDate(settlementDate)
                        .build())
                .collect(Collectors.toList());

        settlementRepository.saveAll(settlementsToSave);

        log.info("Successfully created {} settlement(s) for date: {}", settlementsToSave.size(), settlementDate);
    }
}
