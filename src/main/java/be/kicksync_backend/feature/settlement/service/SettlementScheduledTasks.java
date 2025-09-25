package be.kicksync_backend.feature.settlement.service;

import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.payment.dto.PartnerSettlementDto;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.feature.settlement.entity.SettlementStatus;
import be.kicksync_backend.feature.settlement.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 * * * * ?")
    @SchedulerLock(name = "dailySettlementTask")
    @Transactional
    public void dailySettlement() {
        long startTime = System.currentTimeMillis();
        log.info("일일 정산 배치 작업이 시작되었습니다.");

        LocalDate settlementDate = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = settlementDate.atStartOfDay();
        LocalDateTime endOfDay = settlementDate.atTime(LocalTime.MAX);

        Map<Long, BigDecimal> partnerTotalAmounts = fetchAndAggregatePayments(startOfDay, endOfDay);

        if (partnerTotalAmounts.isEmpty()) {
            log.info("정산할 결제 내역이 없습니다. 날짜: {}", settlementDate);
            return;
        }

        List<Settlement> settlementsToSave = createSettlementEntities(partnerTotalAmounts, settlementDate);

        bulkInsertSettlements(settlementsToSave);

        long duration = System.currentTimeMillis() - startTime;
        log.info("성공적으로 {}건의 정산을 생성했습니다. 대상 날짜: {}. 실행 시간: {}ms",
                settlementsToSave.size(), settlementDate, duration);
    }

    private void bulkInsertSettlements(List<Settlement> settlements) {
        String sql = "INSERT INTO settlements (partner_id, total_amount, settlement_date, status) VALUES (?, ?, ?, ?)";

        final int BATCH_SIZE = 1000;
        for (int from = 0; from < settlements.size(); from += BATCH_SIZE) {
            int to = Math.min(from + BATCH_SIZE, settlements.size());
            List<Settlement> chunk = settlements.subList(from, to);
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Settlement settlement = chunk.get(i);
                    ps.setLong(1, settlement.getPartnerId());
                    ps.setBigDecimal(2, settlement.getTotalAmount());
                    ps.setObject(3, settlement.getSettlementDate());
                    ps.setString(4, String.valueOf(SettlementStatus.COMPLETED));
                }

                @Override
                public int getBatchSize() {
                    return chunk.size();
                }
            });
        }
    }

    private Map<Long, BigDecimal> fetchAndAggregatePayments(LocalDateTime start, LocalDateTime end) {
        List<PartnerSettlementDto> results = paymentRepository.findPartnerTotalsByStatusAndPaymentDateBetween(PaymentStatus.PAID, start, end);
        return results.stream()
                .collect(Collectors.toMap(
                        PartnerSettlementDto::getPartnerId,
                        PartnerSettlementDto::getTotalAmount
                ));
    }

    private List<Settlement> createSettlementEntities(Map<Long, BigDecimal> partnerTotalAmounts, LocalDate settlementDate) {
        return partnerTotalAmounts.entrySet().stream()
                .map(entry -> Settlement.builder()
                        .partnerId(entry.getKey())
                        .totalAmount(entry.getValue())
                        .settlementDate(settlementDate)
                        .build())
                .toList();
    }
}