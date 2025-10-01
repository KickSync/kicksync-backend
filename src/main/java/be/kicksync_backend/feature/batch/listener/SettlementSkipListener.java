package be.kicksync_backend.feature.batch.listener;

import be.kicksync_backend.feature.payment.dto.PartnerSettlementDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementSkipListener implements SkipListener<PartnerSettlementDto, Object> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Reading 중 항목 스킵 발생. 에러: {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("Writing 중 항목 스킵 발생. 항목: {}, 에러: {}", item, t.getMessage());
    }

    @Override
    public void onSkipInProcess(PartnerSettlementDto item, Throwable t) {
        log.warn("Processing 중 항목 스킵 발생. 파트너 ID: {}, 에러: {}",
                item != null ? item.getPartnerId() : "N/A",
                t.getMessage());
    }
} 