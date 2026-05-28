package be.kicksync_backend.feature.settlement.batch;

import be.kicksync_backend.feature.settlement.entity.SettlementErrorLog;
import be.kicksync_backend.feature.settlement.repository.SettlementErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementSkipListener implements SkipListener<SettlementBatchConfig.OrderRowDto, SettlementBatchConfig.OrderRowDto> {

    private final SettlementErrorLogRepository errorLogRepository;

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("[TO-BE] Reading 중 스킵 발생. 에러: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(SettlementBatchConfig.OrderRowDto item, Throwable t) {
        log.warn("[TO-BE] Processing 중 스킵 발생. 주문 ID: {}, 파트너 ID: {}, 에러: {}",
                item != null ? item.getId() : "N/A",
                item != null ? item.getPartnerId() : "N/A",
                t.getMessage());

        if (item != null) {
            saveErrorLog(item, t.getMessage());
        }
    }

    @Override
    public void onSkipInWrite(SettlementBatchConfig.OrderRowDto item, Throwable t) {
        log.warn("[TO-BE] Writing 중 스킵 발생. 주문 ID: {}, 파트너 ID: {}, 에러: {}",
                item != null ? item.getId() : "N/A",
                item != null ? item.getPartnerId() : "N/A",
                t.getMessage());

        if (item != null) {
            saveErrorLog(item, t.getMessage());
        }
    }

    private void saveErrorLog(SettlementBatchConfig.OrderRowDto orderRow, String errorMessage) {
        try {
            SettlementErrorLog errorLog = SettlementErrorLog.builder()
                    .partnerId(orderRow.getPartnerId())
                    .settlementDate(LocalDate.now())
                    .failedAmount(orderRow.getFinalPrice())
                    .errorMessage(errorMessage)
                    .build();
            errorLogRepository.save(errorLog);
            log.info("[TO-BE] DLQ 테이블(settlement_error_logs)에 에러 로그 저장 완료.");
        } catch (Exception e) {
            log.error("[TO-BE] DLQ 에러 로그 저장 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
